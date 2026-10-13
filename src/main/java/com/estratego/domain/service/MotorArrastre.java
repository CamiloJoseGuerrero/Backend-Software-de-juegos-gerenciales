package com.estratego.domain.service;

import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.ImpactoOpcion;
import com.estratego.domain.model.financiero.EstadoFinanciero;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Motor de arrastre del balance entre casos (D2, reglas aprobadas 2026-10-09).
 *
 * <p>Convención del modelo: igual que el balance que escribe el docente, un estado financiero
 * cuadra incluyendo la utilidad del ejercicio (Activo = Pasivo + Capital + Retenidas + Utilidad neta).
 * Por eso cada estado INICIAL ya lleva en efectivo la utilidad proyectada del escenario del caso,
 * y al cerrar solo se mueve la diferencia que produce la decisión. El efecto neto es la regla 2:
 * la utilidad neta del caso termina sumada a efectivo y a utilidades retenidas.</p>
 *
 * <ul>
 *   <li><b>INICIAL</b>: balance (heredado o del docente) + estado de resultados del escenario del caso
 *       (regla 3: fresco, sin impactos acumulados).</li>
 *   <li><b>FINAL</b>: estado de resultados con la decisión aplicada (o la penalización);
 *       efectivo ajustado por la diferencia; flujo operativo = utilidad neta, inversión y financiación = 0
 *       (regla 4).</li>
 *   <li><b>Paso al caso siguiente</b>: la utilidad neta del FINAL pasa a utilidades retenidas;
 *       el resto del balance pasa igual (regla 2).</li>
 * </ul>
 * Efectivo y utilidades retenidas pueden quedar negativos. Todas las salidas cuadran.
 * Clase pura: no guarda nada; la orquestación (cuándo se calcula y se persiste) va en la capa de aplicación.
 */
public final class MotorArrastre {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private MotorArrastre() {
    }

    /**
     * Estado INICIAL de origen DOCENTE.
     * En el primer caso es exactamente el balance del docente (con sus flujos).
     * Para una empresa que entra en un caso posterior: el mismo balance del docente,
     * con el escenario de ese caso en lugar del del primero.
     */
    public static EstadoFinanciero inicialDocente(Caso primerCaso, Caso caso) {
        EstadoFinanciero docente = primerCaso.getFinanciero();
        if (primerCaso == caso || (primerCaso.getId() != null && primerCaso.getId().equals(caso.getId()))) {
            return copia(docente);
        }
        EstadoFinanciero escenario = caso.getFinanciero();
        EstadoFinanciero r = copia(docente);
        conEscenario(r, escenario);
        r.setEfectivo(redondear(valor(docente.getEfectivo())
                .subtract(docente.utilidadNeta())
                .add(escenario.utilidadNeta())));
        return r;
    }

    /** Estado INICIAL de origen HEREDADO: cierra el FINAL del caso anterior y abre el escenario de este caso. */
    public static EstadoFinanciero inicialHeredado(EstadoFinanciero finalAnterior, Caso caso) {
        EstadoFinanciero escenario = caso.getFinanciero();
        EstadoFinanciero r = copia(finalAnterior);
        r.setUtilidadesRetenidas(redondear(valor(finalAnterior.getUtilidadesRetenidas())
                .add(finalAnterior.utilidadNeta())));
        conEscenario(r, escenario);
        r.setEfectivo(redondear(valor(finalAnterior.getEfectivo()).add(escenario.utilidadNeta())));
        return r;
    }

    /** Estado FINAL de una empresa que eligió una opción (impacto null = la opción no mueve nada). */
    public static EstadoFinanciero finalConDecision(EstadoFinanciero inicial, ImpactoOpcion impacto) {
        EstadoFinanciero r = copia(inicial);
        if (impacto != null) {
            r.setVentasNetas(redondear(CalculadoraResultadoCaso.aplicar(inicial.getVentasNetas(), impacto.ventasNetas())));
            r.setCostoVentas(redondear(CalculadoraResultadoCaso.aplicar(inicial.getCostoVentas(), impacto.costoVentas())));
            r.setGastosAdministracion(redondear(CalculadoraResultadoCaso.aplicar(
                    inicial.getGastosAdministracion(), impacto.gastosAdministracion())));
            r.setGastosVentas(redondear(CalculadoraResultadoCaso.aplicar(inicial.getGastosVentas(), impacto.gastosVentas())));
            r.setGastosFinancieros(redondear(CalculadoraResultadoCaso.aplicar(
                    inicial.getGastosFinancieros(), impacto.gastosFinancieros())));
            r.setImpuestoRenta(redondear(CalculadoraResultadoCaso.aplicar(inicial.getImpuestoRenta(), impacto.impuestoRenta())));
        }
        return cerrar(inicial, r);
    }

    /**
     * Estado FINAL de una empresa que no decidió: utilidad − |utilidad| × porcentaje / 100
     * (igual que CalculadoraResultadoCaso.utilidadPenalizada). El castigo se registra como
     * gasto de administración para que el estado de resultados explique la utilidad.
     */
    public static EstadoFinanciero finalPenalizado(EstadoFinanciero inicial, BigDecimal porcentaje) {
        BigDecimal p = porcentaje != null ? porcentaje : BigDecimal.ZERO;
        BigDecimal castigo = redondear(inicial.utilidadNeta().abs().multiply(p).divide(CIEN, 10, RoundingMode.HALF_UP));
        EstadoFinanciero r = copia(inicial);
        r.setGastosAdministracion(redondear(valor(inicial.getGastosAdministracion()).add(castigo)));
        return cerrar(inicial, r);
    }

    /** Efectivo += (utilidad final − utilidad inicial); flujos según la regla 4. */
    private static EstadoFinanciero cerrar(EstadoFinanciero inicial, EstadoFinanciero r) {
        BigDecimal utilidad = r.utilidadNeta();
        BigDecimal diferencia = utilidad.subtract(inicial.utilidadNeta());
        r.setEfectivo(redondear(valor(inicial.getEfectivo()).add(diferencia)));
        r.setFlujoOperativo(redondear(utilidad));
        r.setFlujoInversion(redondear(BigDecimal.ZERO));
        r.setFlujoFinanciacion(redondear(BigDecimal.ZERO));
        return r;
    }

    /** Estado de resultados del escenario del caso; flujo operativo proyectado = su utilidad. */
    private static void conEscenario(EstadoFinanciero r, EstadoFinanciero escenario) {
        r.setVentasNetas(valor(escenario.getVentasNetas()));
        r.setCostoVentas(valor(escenario.getCostoVentas()));
        r.setGastosAdministracion(valor(escenario.getGastosAdministracion()));
        r.setGastosVentas(valor(escenario.getGastosVentas()));
        r.setGastosFinancieros(valor(escenario.getGastosFinancieros()));
        r.setImpuestoRenta(valor(escenario.getImpuestoRenta()));
        r.setFlujoOperativo(redondear(escenario.utilidadNeta()));
        r.setFlujoInversion(redondear(BigDecimal.ZERO));
        r.setFlujoFinanciacion(redondear(BigDecimal.ZERO));
    }

    private static EstadoFinanciero copia(EstadoFinanciero f) {
        return new EstadoFinanciero(
                valor(f.getEfectivo()), valor(f.getCuentasPorCobrar()), valor(f.getInventarios()),
                valor(f.getPropiedadPlantaEquipo()), valor(f.getActivosIntangibles()),
                valor(f.getCuentasPorPagar()), valor(f.getObligacionesFinancierasCortoPlazo()),
                valor(f.getObligacionesFinancierasLargoPlazo()),
                valor(f.getCapitalSocial()), valor(f.getUtilidadesRetenidas()),
                valor(f.getVentasNetas()), valor(f.getCostoVentas()), valor(f.getGastosAdministracion()),
                valor(f.getGastosVentas()), valor(f.getGastosFinancieros()), valor(f.getImpuestoRenta()),
                valor(f.getFlujoOperativo()), valor(f.getFlujoInversion()), valor(f.getFlujoFinanciacion()));
    }

    private static BigDecimal valor(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static BigDecimal redondear(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}

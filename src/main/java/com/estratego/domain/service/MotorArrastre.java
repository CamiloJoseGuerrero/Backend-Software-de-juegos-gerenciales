package com.estratego.domain.service;

import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.ImpactoOpcion;
import com.estratego.domain.model.financiero.EstadoFinanciero;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MotorArrastre {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private MotorArrastre() {
    }

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

    public static EstadoFinanciero inicialHeredado(EstadoFinanciero finalAnterior, Caso caso) {
        EstadoFinanciero escenario = caso.getFinanciero();
        EstadoFinanciero r = copia(finalAnterior);
        r.setUtilidadesRetenidas(redondear(valor(finalAnterior.getUtilidadesRetenidas())
                .add(finalAnterior.utilidadNeta())));
        conEscenario(r, escenario);
        r.setEfectivo(redondear(valor(finalAnterior.getEfectivo()).add(escenario.utilidadNeta())));
        return r;
    }

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

    public static EstadoFinanciero finalPenalizado(EstadoFinanciero inicial, BigDecimal porcentaje) {
        BigDecimal p = porcentaje != null ? porcentaje : BigDecimal.ZERO;
        BigDecimal castigo = redondear(inicial.utilidadNeta().abs().multiply(p).divide(CIEN, 10, RoundingMode.HALF_UP));
        EstadoFinanciero r = copia(inicial);
        r.setGastosAdministracion(redondear(valor(inicial.getGastosAdministracion()).add(castigo)));
        return cerrar(inicial, r);
    }

    private static EstadoFinanciero cerrar(EstadoFinanciero inicial, EstadoFinanciero r) {
        BigDecimal utilidad = r.utilidadNeta();
        BigDecimal diferencia = utilidad.subtract(inicial.utilidadNeta());
        r.setEfectivo(redondear(valor(inicial.getEfectivo()).add(diferencia)));
        r.setFlujoOperativo(redondear(utilidad));
        r.setFlujoInversion(redondear(BigDecimal.ZERO));
        r.setFlujoFinanciacion(redondear(BigDecimal.ZERO));
        return r;
    }

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

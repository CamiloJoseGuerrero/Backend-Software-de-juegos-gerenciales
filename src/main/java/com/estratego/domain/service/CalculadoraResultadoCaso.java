package com.estratego.domain.service;

import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.ImpactoDriver;
import com.estratego.domain.model.caso.ImpactoOpcion;
import com.estratego.domain.model.caso.TipoImpacto;
import com.estratego.domain.model.financiero.EstadoFinanciero;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Motor mínimo: utilidad neta de una empresa en un caso.
 * <ul>
 *   <li>Decidió: al estado de resultados del caso se le aplica el impacto de la opción elegida.</li>
 *   <li>No decidió: la utilidad del caso menos |utilidad| × penalizacionMax %
 *       (con |..| una pérdida se vuelve más grande, nunca más chica).</li>
 * </ul>
 * El balance no se arrastra entre casos (pendiente de decisión del cliente, D2).
 */
public final class CalculadoraResultadoCaso {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private CalculadoraResultadoCaso() {
    }

    /** Utilidad neta del caso para una empresa que eligió una opción con este impacto (puede ser null). */
    public static BigDecimal utilidadConDecision(Caso caso, ImpactoOpcion impacto) {
        EstadoFinanciero ef = caso.getFinanciero();
        if (ef == null || impacto == null) {
            // Casos anteriores al modelo de 19 partidas no tienen estado de resultados al que aplicar el impacto
            return redondear(utilidadBase(caso));
        }
        BigDecimal ventas = aplicar(ef.getVentasNetas(), impacto.ventasNetas());
        BigDecimal costo = aplicar(ef.getCostoVentas(), impacto.costoVentas());
        BigDecimal admin = aplicar(ef.getGastosAdministracion(), impacto.gastosAdministracion());
        BigDecimal gVentas = aplicar(ef.getGastosVentas(), impacto.gastosVentas());
        BigDecimal financieros = aplicar(ef.getGastosFinancieros(), impacto.gastosFinancieros());
        BigDecimal impuesto = aplicar(ef.getImpuestoRenta(), impacto.impuestoRenta());
        return redondear(ventas.subtract(costo).subtract(admin).subtract(gVentas)
                .subtract(financieros).subtract(impuesto));
    }

    /** Utilidad neta del caso para una empresa que no decidió. */
    public static BigDecimal utilidadPenalizada(Caso caso) {
        BigDecimal base = utilidadBase(caso);
        BigDecimal castigo = base.abs().multiply(porcentajePenalizacion(caso)).divide(CIEN, 10, RoundingMode.HALF_UP);
        return redondear(base.subtract(castigo));
    }

    /** Siempre el máximo del rango: predecible, igual para todas y desincentiva no decidir. */
    public static BigDecimal porcentajePenalizacion(Caso caso) {
        return caso.getPenalizacionMax() != null ? caso.getPenalizacionMax() : BigDecimal.ZERO;
    }

    /** Utilidad neta del caso sin decisión ni penalización (la que escribió el docente). */
    public static BigDecimal utilidadBase(Caso caso) {
        if (caso.getUtilidadNeta() != null) return caso.getUtilidadNeta();
        return caso.getFinanciero() != null ? caso.getFinanciero().utilidadNeta() : BigDecimal.ZERO;
    }

    /** porcentaje: rubro × (1 + valor/100); monto: rubro + valor. Un rubro nunca queda negativo. */
    static BigDecimal aplicar(BigDecimal rubro, ImpactoDriver driver) {
        BigDecimal base = rubro != null ? rubro : BigDecimal.ZERO;
        if (driver == null || driver.valor() == null || driver.tipo() == null) return base;
        BigDecimal resultado = driver.tipo() == TipoImpacto.PORCENTAJE
                ? base.add(base.multiply(driver.valor()).divide(CIEN, 10, RoundingMode.HALF_UP))
                : base.add(driver.valor());
        return resultado.signum() < 0 ? BigDecimal.ZERO : resultado;
    }

    private static BigDecimal redondear(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}

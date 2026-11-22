package com.estratego.domain.service;

import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.ImpactoDriver;
import com.estratego.domain.model.caso.ImpactoOpcion;
import com.estratego.domain.model.caso.TipoImpacto;
import com.estratego.domain.model.financiero.EstadoFinanciero;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CalculadoraResultadoCaso {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private CalculadoraResultadoCaso() {
    }

    public static BigDecimal utilidadConDecision(Caso caso, ImpactoOpcion impacto) {
        EstadoFinanciero ef = caso.getFinanciero();
        if (ef == null || impacto == null) {
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

    public static BigDecimal utilidadPenalizada(Caso caso) {
        BigDecimal base = utilidadBase(caso);
        BigDecimal castigo = base.abs().multiply(porcentajePenalizacion(caso)).divide(CIEN, 10, RoundingMode.HALF_UP);
        return redondear(base.subtract(castigo));
    }

    public static BigDecimal porcentajePenalizacion(Caso caso) {
        return caso.getPenalizacionMax() != null ? caso.getPenalizacionMax() : BigDecimal.ZERO;
    }

    public static BigDecimal utilidadBase(Caso caso) {
        if (caso.getUtilidadNeta() != null) return caso.getUtilidadNeta();
        return caso.getFinanciero() != null ? caso.getFinanciero().utilidadNeta() : BigDecimal.ZERO;
    }

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

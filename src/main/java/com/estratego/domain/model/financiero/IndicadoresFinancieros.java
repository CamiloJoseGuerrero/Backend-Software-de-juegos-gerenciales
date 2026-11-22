package com.estratego.domain.model.financiero;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record IndicadoresFinancieros(
        BigDecimal razonCorriente,
        BigDecimal pruebaAcida,
        BigDecimal endeudamiento,
        BigDecimal margenNeto,
        BigDecimal roa,
        BigDecimal roe) {

    private static final int DECIMALES = 4;

    public static IndicadoresFinancieros de(EstadoFinanciero ef) {
        if (ef == null) return null;
        BigDecimal activoCorriente = v(ef.getEfectivo()).add(v(ef.getCuentasPorCobrar())).add(v(ef.getInventarios()));
        BigDecimal pasivoCorriente = v(ef.getCuentasPorPagar()).add(v(ef.getObligacionesFinancierasCortoPlazo()));
        BigDecimal utilidad = ef.utilidadNeta();
        BigDecimal activoTotal = ef.activoTotal();
        BigDecimal patrimonio = ef.patrimonio();
        return new IndicadoresFinancieros(
                dividir(activoCorriente, pasivoCorriente),
                dividir(activoCorriente.subtract(v(ef.getInventarios())), pasivoCorriente),
                dividir(ef.pasivoTotal(), activoTotal),
                dividir(utilidad, v(ef.getVentasNetas())),
                dividir(utilidad, activoTotal),
                patrimonio.signum() > 0 ? dividir(utilidad, patrimonio) : null);
    }

    private static BigDecimal dividir(BigDecimal a, BigDecimal b) {
        if (b == null || b.signum() == 0) return null;
        return a.divide(b, DECIMALES, RoundingMode.HALF_UP);
    }

    private static BigDecimal v(BigDecimal x) {
        return x != null ? x : BigDecimal.ZERO;
    }
}

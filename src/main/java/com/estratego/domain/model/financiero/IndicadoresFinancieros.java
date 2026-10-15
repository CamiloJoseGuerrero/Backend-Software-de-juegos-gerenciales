package com.estratego.domain.model.financiero;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Razones financieras de un estado financiero (opción A). Todas como fracción (0.34 = 34 %; el frontend
 * las formatea), con 4 decimales. Un indicador va en null si su denominador es 0, y el ROE también si el
 * patrimonio es negativo (no tiene lectura).
 *
 * <ul>
 *   <li>Activo corriente = efectivo + cuentas por cobrar + inventarios.</li>
 *   <li>Pasivo corriente = cuentas por pagar + obligaciones financieras de corto plazo.</li>
 *   <li>razonCorriente = activo corriente / pasivo corriente.</li>
 *   <li>pruebaAcida = (activo corriente − inventarios) / pasivo corriente.</li>
 *   <li>endeudamiento = pasivo total / activo total.</li>
 *   <li>margenNeto = utilidad neta / ventas netas.</li>
 *   <li>roa = utilidad neta / activo total.</li>
 *   <li>roe = utilidad neta / patrimonio (el patrimonio incluye la utilidad del ejercicio, como en
 *       {@link EstadoFinanciero#patrimonio()}).</li>
 * </ul>
 */
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

package com.estratego.domain.model.financiero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Estado financiero de una empresa: balance, estado de resultados y flujo de efectivo.
 * Los totales no se guardan aquí: se calculan a partir de las partidas.
 * Lo usa el Caso (situación inicial) y lo podrá usar el motor para cada período.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoFinanciero {

    // Balance - Activo
    private BigDecimal efectivo;
    private BigDecimal cuentasPorCobrar;
    private BigDecimal inventarios;
    private BigDecimal propiedadPlantaEquipo;
    private BigDecimal activosIntangibles;

    // Balance - Pasivo
    private BigDecimal cuentasPorPagar;
    private BigDecimal obligacionesFinancierasCortoPlazo;
    private BigDecimal obligacionesFinancierasLargoPlazo;

    // Balance - Patrimonio (sin la utilidad del ejercicio)
    private BigDecimal capitalSocial;
    private BigDecimal utilidadesRetenidas;

    // Estado de Resultados
    private BigDecimal ventasNetas;
    private BigDecimal costoVentas;
    private BigDecimal gastosAdministracion;
    private BigDecimal gastosVentas;
    private BigDecimal gastosFinancieros;
    private BigDecimal impuestoRenta;

    // Flujo de Efectivo
    private BigDecimal flujoOperativo;
    private BigDecimal flujoInversion;
    private BigDecimal flujoFinanciacion;

    public BigDecimal activoTotal() {
        return suma(efectivo, cuentasPorCobrar, inventarios, propiedadPlantaEquipo, activosIntangibles);
    }

    public BigDecimal pasivoTotal() {
        return suma(cuentasPorPagar, obligacionesFinancierasCortoPlazo, obligacionesFinancierasLargoPlazo);
    }

    public BigDecimal utilidadNeta() {
        return valor(ventasNetas)
                .subtract(suma(costoVentas, gastosAdministracion, gastosVentas, gastosFinancieros, impuestoRenta));
    }

    /** Capital + utilidades retenidas + utilidad del ejercicio. */
    public BigDecimal patrimonio() {
        return suma(capitalSocial, utilidadesRetenidas).add(utilidadNeta());
    }

    /** Ecuación contable: Activo = Pasivo + Patrimonio. */
    public boolean cuadra() {
        return activoTotal().compareTo(pasivoTotal().add(patrimonio())) == 0;
    }

    private static BigDecimal suma(BigDecimal... valores) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal v : valores) total = total.add(valor(v));
        return total;
    }

    private static BigDecimal valor(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}

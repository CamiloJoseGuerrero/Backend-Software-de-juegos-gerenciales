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

    /** Nombres (como en el JSON) de las partidas que vienen en null, en el orden del modelo. */
    public java.util.List<String> partidasFaltantes() {
        java.util.List<String> faltan = new java.util.ArrayList<>();
        java.util.Map<String, BigDecimal> partidas = new java.util.LinkedHashMap<>();
        partidas.put("efectivo", efectivo);
        partidas.put("cuentasPorCobrar", cuentasPorCobrar);
        partidas.put("inventarios", inventarios);
        partidas.put("propiedadPlantaEquipo", propiedadPlantaEquipo);
        partidas.put("activosIntangibles", activosIntangibles);
        partidas.put("cuentasPorPagar", cuentasPorPagar);
        partidas.put("obligacionesFinancierasCortoPlazo", obligacionesFinancierasCortoPlazo);
        partidas.put("obligacionesFinancierasLargoPlazo", obligacionesFinancierasLargoPlazo);
        partidas.put("capitalSocial", capitalSocial);
        partidas.put("utilidadesRetenidas", utilidadesRetenidas);
        partidas.put("ventasNetas", ventasNetas);
        partidas.put("costoVentas", costoVentas);
        partidas.put("gastosAdministracion", gastosAdministracion);
        partidas.put("gastosVentas", gastosVentas);
        partidas.put("gastosFinancieros", gastosFinancieros);
        partidas.put("impuestoRenta", impuestoRenta);
        partidas.put("flujoOperativo", flujoOperativo);
        partidas.put("flujoInversion", flujoInversion);
        partidas.put("flujoFinanciacion", flujoFinanciacion);
        partidas.forEach((nombre, v) -> { if (v == null) faltan.add(nombre); });
        return faltan;
    }

    /** true si vienen las 10 partidas del balance (activo, pasivo y patrimonio). */
    public boolean balanceCompleto() {
        return efectivo != null && cuentasPorCobrar != null && inventarios != null
                && propiedadPlantaEquipo != null && activosIntangibles != null
                && cuentasPorPagar != null && obligacionesFinancierasCortoPlazo != null
                && obligacionesFinancierasLargoPlazo != null
                && capitalSocial != null && utilidadesRetenidas != null;
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

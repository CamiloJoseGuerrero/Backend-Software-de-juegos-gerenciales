package com.estratego.application.dto.docente;

import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Información financiera inicial del caso ("período 0").
 * En el request vienen las 19 partidas; los 4 totales los calcula el backend
 * (si el cliente los manda, se ignoran) y solo salen en la respuesta.
 */
@Data
@NoArgsConstructor
public class FinancieroCaso {

    // ---- Totales calculados (solo lectura) ----

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal activoTotal;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal pasivoTotal;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal patrimonio;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal utilidadNeta;

    // ---- Balance - Activo ----

    @NotNull(message = "El efectivo es obligatorio")
    @DecimalMin(value = "0", message = "El efectivo no puede ser negativo")
    private BigDecimal efectivo;

    @NotNull(message = "Las cuentas por cobrar son obligatorias")
    @DecimalMin(value = "0", message = "Las cuentas por cobrar no pueden ser negativas")
    private BigDecimal cuentasPorCobrar;

    @NotNull(message = "Los inventarios son obligatorios")
    @DecimalMin(value = "0", message = "Los inventarios no pueden ser negativos")
    private BigDecimal inventarios;

    @NotNull(message = "La propiedad, planta y equipo es obligatoria")
    @DecimalMin(value = "0", message = "La propiedad, planta y equipo no puede ser negativa")
    private BigDecimal propiedadPlantaEquipo;

    @NotNull(message = "Los activos intangibles son obligatorios")
    @DecimalMin(value = "0", message = "Los activos intangibles no pueden ser negativos")
    private BigDecimal activosIntangibles;

    // ---- Balance - Pasivo ----

    @NotNull(message = "Las cuentas por pagar son obligatorias")
    @DecimalMin(value = "0", message = "Las cuentas por pagar no pueden ser negativas")
    private BigDecimal cuentasPorPagar;

    @NotNull(message = "Las obligaciones financieras de corto plazo son obligatorias")
    @DecimalMin(value = "0", message = "Las obligaciones financieras de corto plazo no pueden ser negativas")
    private BigDecimal obligacionesFinancierasCortoPlazo;

    @NotNull(message = "Las obligaciones financieras de largo plazo son obligatorias")
    @DecimalMin(value = "0", message = "Las obligaciones financieras de largo plazo no pueden ser negativas")
    private BigDecimal obligacionesFinancierasLargoPlazo;

    // ---- Balance - Patrimonio ----

    @NotNull(message = "El capital social es obligatorio")
    @DecimalMin(value = "0", message = "El capital social no puede ser negativo")
    private BigDecimal capitalSocial;

    /** Puede ser negativo (pérdidas acumuladas). */
    @NotNull(message = "Las utilidades retenidas son obligatorias")
    private BigDecimal utilidadesRetenidas;

    // ---- Estado de Resultados ----

    @NotNull(message = "Las ventas netas son obligatorias")
    @DecimalMin(value = "0", message = "Las ventas netas no pueden ser negativas")
    private BigDecimal ventasNetas;

    @NotNull(message = "El costo de ventas es obligatorio")
    @DecimalMin(value = "0", message = "El costo de ventas no puede ser negativo")
    private BigDecimal costoVentas;

    @NotNull(message = "Los gastos de administración son obligatorios")
    @DecimalMin(value = "0", message = "Los gastos de administración no pueden ser negativos")
    private BigDecimal gastosAdministracion;

    @NotNull(message = "Los gastos de ventas son obligatorios")
    @DecimalMin(value = "0", message = "Los gastos de ventas no pueden ser negativos")
    private BigDecimal gastosVentas;

    @NotNull(message = "Los gastos financieros son obligatorios")
    @DecimalMin(value = "0", message = "Los gastos financieros no pueden ser negativos")
    private BigDecimal gastosFinancieros;

    @NotNull(message = "El impuesto de renta es obligatorio")
    @DecimalMin(value = "0", message = "El impuesto de renta no puede ser negativo")
    private BigDecimal impuestoRenta;

    // ---- Flujo de Efectivo (pueden ser negativos) ----

    @NotNull(message = "El flujo operativo es obligatorio")
    private BigDecimal flujoOperativo;

    @NotNull(message = "El flujo de inversión es obligatorio")
    private BigDecimal flujoInversion;

    @NotNull(message = "El flujo de financiación es obligatorio")
    private BigDecimal flujoFinanciacion;

    /** Las 19 partidas del request como modelo de dominio. */
    public EstadoFinanciero aEstadoFinanciero() {
        return new EstadoFinanciero(
                efectivo, cuentasPorCobrar, inventarios, propiedadPlantaEquipo, activosIntangibles,
                cuentasPorPagar, obligacionesFinancierasCortoPlazo, obligacionesFinancierasLargoPlazo,
                capitalSocial, utilidadesRetenidas,
                ventasNetas, costoVentas, gastosAdministracion, gastosVentas, gastosFinancieros, impuestoRenta,
                flujoOperativo, flujoInversion, flujoFinanciacion);
    }

    /** Respuesta: totales guardados del caso + sus partidas (null en casos anteriores al rediseño). */
    public static FinancieroCaso de(Caso c) {
        FinancieroCaso f = new FinancieroCaso();
        f.activoTotal = c.getActivoTotal();
        f.pasivoTotal = c.getPasivoTotal();
        f.patrimonio = c.getPatrimonio();
        f.utilidadNeta = c.getUtilidadNeta();
        EstadoFinanciero e = c.getFinanciero();
        if (e != null) {
            f.efectivo = e.getEfectivo();
            f.cuentasPorCobrar = e.getCuentasPorCobrar();
            f.inventarios = e.getInventarios();
            f.propiedadPlantaEquipo = e.getPropiedadPlantaEquipo();
            f.activosIntangibles = e.getActivosIntangibles();
            f.cuentasPorPagar = e.getCuentasPorPagar();
            f.obligacionesFinancierasCortoPlazo = e.getObligacionesFinancierasCortoPlazo();
            f.obligacionesFinancierasLargoPlazo = e.getObligacionesFinancierasLargoPlazo();
            f.capitalSocial = e.getCapitalSocial();
            f.utilidadesRetenidas = e.getUtilidadesRetenidas();
            f.ventasNetas = e.getVentasNetas();
            f.costoVentas = e.getCostoVentas();
            f.gastosAdministracion = e.getGastosAdministracion();
            f.gastosVentas = e.getGastosVentas();
            f.gastosFinancieros = e.getGastosFinancieros();
            f.impuestoRenta = e.getImpuestoRenta();
            f.flujoOperativo = e.getFlujoOperativo();
            f.flujoInversion = e.getFlujoInversion();
            f.flujoFinanciacion = e.getFlujoFinanciacion();
        }
        return f;
    }
}

package com.estratego.application.dto.docente;

import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class FinancieroCaso {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal activoTotal;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal pasivoTotal;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal patrimonio;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal utilidadNeta;

    @DecimalMin(value = "0", message = "El efectivo no puede ser negativo")
    private BigDecimal efectivo;

    @DecimalMin(value = "0", message = "Las cuentas por cobrar no pueden ser negativas")
    private BigDecimal cuentasPorCobrar;

    @DecimalMin(value = "0", message = "Los inventarios no pueden ser negativos")
    private BigDecimal inventarios;

    @DecimalMin(value = "0", message = "La propiedad, planta y equipo no puede ser negativa")
    private BigDecimal propiedadPlantaEquipo;

    @DecimalMin(value = "0", message = "Los activos intangibles no pueden ser negativos")
    private BigDecimal activosIntangibles;

    @DecimalMin(value = "0", message = "Las cuentas por pagar no pueden ser negativas")
    private BigDecimal cuentasPorPagar;

    @DecimalMin(value = "0", message = "Las obligaciones financieras de corto plazo no pueden ser negativas")
    private BigDecimal obligacionesFinancierasCortoPlazo;

    @DecimalMin(value = "0", message = "Las obligaciones financieras de largo plazo no pueden ser negativas")
    private BigDecimal obligacionesFinancierasLargoPlazo;

    @DecimalMin(value = "0", message = "El capital social no puede ser negativo")
    private BigDecimal capitalSocial;

    private BigDecimal utilidadesRetenidas;

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

    private BigDecimal flujoOperativo;

    private BigDecimal flujoInversion;

    private BigDecimal flujoFinanciacion;

    public EstadoFinanciero aEstadoFinanciero() {
        return new EstadoFinanciero(
                efectivo, cuentasPorCobrar, inventarios, propiedadPlantaEquipo, activosIntangibles,
                cuentasPorPagar, obligacionesFinancierasCortoPlazo, obligacionesFinancierasLargoPlazo,
                capitalSocial, utilidadesRetenidas,
                ventasNetas, costoVentas, gastosAdministracion, gastosVentas, gastosFinancieros, impuestoRenta,
                flujoOperativo, flujoInversion, flujoFinanciacion);
    }

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

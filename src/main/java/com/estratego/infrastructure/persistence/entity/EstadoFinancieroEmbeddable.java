package com.estratego.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Las 19 partidas del estado financiero como columnas de la tabla caso.
 * Requiere db/migraciones/2026-10-02b_caso_modelo_financiero.sql.
 * Si todas son NULL (casos anteriores al rediseño), Hibernate deja el objeto en null.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoFinancieroEmbeddable {

    @Column(name = "efectivo", precision = 18, scale = 2)
    private BigDecimal efectivo;
    @Column(name = "cuentas_por_cobrar", precision = 18, scale = 2)
    private BigDecimal cuentasPorCobrar;
    @Column(name = "inventarios", precision = 18, scale = 2)
    private BigDecimal inventarios;
    @Column(name = "propiedad_planta_equipo", precision = 18, scale = 2)
    private BigDecimal propiedadPlantaEquipo;
    @Column(name = "activos_intangibles", precision = 18, scale = 2)
    private BigDecimal activosIntangibles;

    @Column(name = "cuentas_por_pagar", precision = 18, scale = 2)
    private BigDecimal cuentasPorPagar;
    @Column(name = "obligaciones_financieras_corto_plazo", precision = 18, scale = 2)
    private BigDecimal obligacionesFinancierasCortoPlazo;
    @Column(name = "obligaciones_financieras_largo_plazo", precision = 18, scale = 2)
    private BigDecimal obligacionesFinancierasLargoPlazo;

    @Column(name = "capital_social", precision = 18, scale = 2)
    private BigDecimal capitalSocial;
    @Column(name = "utilidades_retenidas", precision = 18, scale = 2)
    private BigDecimal utilidadesRetenidas;

    @Column(name = "ventas_netas", precision = 18, scale = 2)
    private BigDecimal ventasNetas;
    @Column(name = "costo_ventas", precision = 18, scale = 2)
    private BigDecimal costoVentas;
    @Column(name = "gastos_administracion", precision = 18, scale = 2)
    private BigDecimal gastosAdministracion;
    @Column(name = "gastos_ventas", precision = 18, scale = 2)
    private BigDecimal gastosVentas;
    @Column(name = "gastos_financieros", precision = 18, scale = 2)
    private BigDecimal gastosFinancieros;
    @Column(name = "impuesto_renta", precision = 18, scale = 2)
    private BigDecimal impuestoRenta;

    @Column(name = "flujo_operativo", precision = 18, scale = 2)
    private BigDecimal flujoOperativo;
    @Column(name = "flujo_inversion", precision = 18, scale = 2)
    private BigDecimal flujoInversion;
    @Column(name = "flujo_financiacion", precision = 18, scale = 2)
    private BigDecimal flujoFinanciacion;
}

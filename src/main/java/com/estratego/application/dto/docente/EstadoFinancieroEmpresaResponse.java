package com.estratego.application.dto.docente;

import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.IndicadoresFinancieros;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoFinancieroEmpresaResponse {
    
    private String origen;
   
    private BigDecimal penalizacionPorcentaje;
    
    private BigDecimal activoTotal;
    private BigDecimal pasivoTotal;
    private BigDecimal patrimonio;
    private BigDecimal utilidadNeta;
  
    private BigDecimal efectivo;
    private BigDecimal cuentasPorCobrar;
    private BigDecimal inventarios;
    private BigDecimal propiedadPlantaEquipo;
    private BigDecimal activosIntangibles;
   
    private BigDecimal cuentasPorPagar;
    private BigDecimal obligacionesFinancierasCortoPlazo;
    private BigDecimal obligacionesFinancierasLargoPlazo;
    
    private BigDecimal capitalSocial;
    private BigDecimal utilidadesRetenidas;
    
    private BigDecimal ventasNetas;
    private BigDecimal costoVentas;
    private BigDecimal gastosAdministracion;
    private BigDecimal gastosVentas;
    private BigDecimal gastosFinancieros;
    private BigDecimal impuestoRenta;
   
    private BigDecimal flujoOperativo;
    private BigDecimal flujoInversion;
    private BigDecimal flujoFinanciacion;
    
    private IndicadoresFinancieros indicadores;

        /**
     * Construye la respuesta a partir del modelo de dominio EstadoFinancieroEmpresa.
     */
    public static EstadoFinancieroEmpresaResponse de(EstadoFinancieroEmpresa e) {
        if (e == null) {
            return null;
        }

        EstadoFinancieroEmpresaResponse r = new EstadoFinancieroEmpresaResponse();

        // 1. Origen en minúsculas (docente, heredado, decision, penalizacion)
        r.origen = e.getOrigen() != null ? e.getOrigen().name().toLowerCase() : null;

        // 2. Porcentaje de penalización
        r.penalizacionPorcentaje = e.getPenalizacionPorcentaje();

        // 3. Partidas, totales e indicadores desde el objeto EstadoFinanciero
        EstadoFinanciero ef = e.getEstado();
        if (ef != null) {
            // Totales calculados con los métodos de EstadoFinanciero
            r.activoTotal = ef.activoTotal();
            r.pasivoTotal = ef.pasivoTotal();
            r.patrimonio = ef.patrimonio();
            r.utilidadNeta = ef.utilidadNeta();

            // Indicadores (record serializable directamente por Jackson)
            r.indicadores = IndicadoresFinancieros.de(ef);

            // Copia de las 19 partidas
            r.efectivo = ef.getEfectivo();
            r.cuentasPorCobrar = ef.getCuentasPorCobrar();
            r.inventarios = ef.getInventarios();
            r.propiedadPlantaEquipo = ef.getPropiedadPlantaEquipo();
            r.activosIntangibles = ef.getActivosIntangibles();
            r.cuentasPorPagar = ef.getCuentasPorPagar();
            r.obligacionesFinancierasCortoPlazo = ef.getObligacionesFinancierasCortoPlazo();
            r.obligacionesFinancierasLargoPlazo = ef.getObligacionesFinancierasLargoPlazo();
            r.capitalSocial = ef.getCapitalSocial();
            r.utilidadesRetenidas = ef.getUtilidadesRetenidas();
            r.ventasNetas = ef.getVentasNetas();
            r.costoVentas = ef.getCostoVentas();
            r.gastosAdministracion = ef.getGastosAdministracion();
            r.gastosVentas = ef.getGastosVentas();
            r.gastosFinancieros = ef.getGastosFinancieros();
            r.impuestoRenta = ef.getImpuestoRenta();
            r.flujoOperativo = ef.getFlujoOperativo();
            r.flujoInversion = ef.getFlujoInversion();
            r.flujoFinanciacion = ef.getFlujoFinanciacion();
        }

        return r;
    }
}

package com.estratego.application.dto.docente;

import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.model.financiero.OrigenEstado;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EstadoFinancieroEmpresaResponseTest {

    @Test
    void deConNullRetornaNull() {
        assertNull(EstadoFinancieroEmpresaResponse.de(null));
    }

    @Test
    void deMapeaOrigenMinusculaTotalesPartidasEIndicadores() {
        EstadoFinanciero ef = new EstadoFinanciero();
        ef.setEfectivo(new BigDecimal("100000"));
        ef.setCuentasPorCobrar(new BigDecimal("50000"));
        ef.setInventarios(new BigDecimal("50000"));
        ef.setPropiedadPlantaEquipo(new BigDecimal("200000"));
        ef.setActivosIntangibles(BigDecimal.ZERO);

        ef.setCuentasPorPagar(new BigDecimal("80000"));
        ef.setObligacionesFinancierasCortoPlazo(new BigDecimal("20000"));
        ef.setObligacionesFinancierasLargoPlazo(new BigDecimal("100000"));

        ef.setCapitalSocial(new BigDecimal("150000"));
        ef.setUtilidadesRetenidas(BigDecimal.ZERO);

        ef.setVentasNetas(new BigDecimal("120000"));
        ef.setCostoVentas(new BigDecimal("40000"));
        ef.setGastosAdministracion(new BigDecimal("10000"));
        ef.setGastosVentas(new BigDecimal("10000"));
        ef.setGastosFinancieros(new BigDecimal("5000"));
        ef.setImpuestoRenta(new BigDecimal("5000")); // utilidadNeta = 120000 - 70000 = 50000

        ef.setFlujoOperativo(new BigDecimal("50000"));
        ef.setFlujoInversion(BigDecimal.ZERO);
        ef.setFlujoFinanciacion(BigDecimal.ZERO);

        EstadoFinancieroEmpresa ee = new EstadoFinancieroEmpresa(
                1L, 10L, 20L,
                MomentoEstado.FINAL,
                OrigenEstado.PENALIZACION,
                null,
                ef,
                LocalDateTime.now(),
                new BigDecimal("5.25")
        );

        EstadoFinancieroEmpresaResponse res = EstadoFinancieroEmpresaResponse.de(ee);

        assertNotNull(res);
        assertEquals("penalizacion", res.getOrigen());
        assertEquals(new BigDecimal("5.25"), res.getPenalizacionPorcentaje());

        // Totales calculados
        assertEquals(new BigDecimal("400000"), res.getActivoTotal());
        assertEquals(new BigDecimal("200000"), res.getPasivoTotal());
        assertEquals(new BigDecimal("50000"), res.getUtilidadNeta());
        assertEquals(new BigDecimal("200000"), res.getPatrimonio()); // 150000 + 0 + 50000

        // 19 partidas
        assertEquals(new BigDecimal("100000"), res.getEfectivo());
        assertEquals(new BigDecimal("120000"), res.getVentasNetas());

        // Indicadores calculados
        assertNotNull(res.getIndicadores());
        assertNotNull(res.getIndicadores().razonCorriente());
        assertNotNull(res.getIndicadores().endeudamiento());
    }
}

package com.estratego.domain.model.financiero;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class IndicadoresFinancierosTest {

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    /**
     * Activo 1.000.000 (corriente 600.000: efectivo 200.000 + cxc 150.000 + inventarios 250.000),
     * pasivo 400.000 (corriente 250.000), patrimonio 600.000 (incluye utilidad 100.000), ventas 800.000.
     */
    private static EstadoFinanciero estado() {
        return new EstadoFinanciero(
                bd("200000"), bd("150000"), bd("250000"), bd("350000"), bd("50000"),
                bd("150000"), bd("100000"), bd("150000"),
                bd("400000"), bd("100000"),
                bd("800000"), bd("500000"), bd("80000"), bd("60000"), bd("20000"), bd("40000"),
                bd("120000"), bd("-80000"), bd("-20000"));
    }

    @Test
    void calculaLasSeisRazonesComoFraccion() {
        IndicadoresFinancieros i = IndicadoresFinancieros.de(estado());

        assertEquals(bd("2.4000"), i.razonCorriente()); // 600.000 / 250.000
        assertEquals(bd("1.4000"), i.pruebaAcida());    // 350.000 / 250.000
        assertEquals(bd("0.4000"), i.endeudamiento());  // 400.000 / 1.000.000
        assertEquals(bd("0.1250"), i.margenNeto());     // 100.000 / 800.000
        assertEquals(bd("0.1000"), i.roa());            // 100.000 / 1.000.000
        assertEquals(bd("0.1667"), i.roe());            // 100.000 / 600.000 (patrimonio con la utilidad)
    }

    @Test
    void conDenominadorEnCeroElIndicadorVaEnNull() {
        EstadoFinanciero ef = estado();
        ef.setCuentasPorPagar(bd("0"));
        ef.setObligacionesFinancierasCortoPlazo(bd("0"));
        ef.setVentasNetas(bd("0"));

        IndicadoresFinancieros i = IndicadoresFinancieros.de(ef);

        assertNull(i.razonCorriente());
        assertNull(i.pruebaAcida());
        assertNull(i.margenNeto());
        assertNotNull(i.endeudamiento());
    }

    @Test
    void conPatrimonioNegativoElRoeVaEnNull() {
        EstadoFinanciero ef = estado();
        ef.setUtilidadesRetenidas(bd("-900000")); // patrimonio = 400.000 − 900.000 + utilidad

        IndicadoresFinancieros i = IndicadoresFinancieros.de(ef);

        assertTrue(ef.patrimonio().signum() < 0);
        assertNull(i.roe());
        assertNotNull(i.roa());
    }

    @Test
    void sinEstadoNoHayIndicadores() {
        assertNull(IndicadoresFinancieros.de(null));
    }
}

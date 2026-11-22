package com.estratego.domain.service;

import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.ImpactoDriver;
import com.estratego.domain.model.caso.ImpactoOpcion;
import com.estratego.domain.model.caso.TipoImpacto;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraResultadoCasoTest {

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    private Caso caso() {
        EstadoFinanciero ef = new EstadoFinanciero();
        ef.setVentasNetas(bd("800000"));
        ef.setCostoVentas(bd("500000"));
        ef.setGastosAdministracion(bd("80000"));
        ef.setGastosVentas(bd("60000"));
        ef.setGastosFinancieros(bd("20000"));
        ef.setImpuestoRenta(bd("40000"));
        Caso c = new Caso();
        c.setFinanciero(ef);
        c.setUtilidadNeta(ef.utilidadNeta());
        c.setPenalizacionMin(bd("2"));
        c.setPenalizacionMax(bd("8"));
        return c;
    }

    private static ImpactoDriver pct(String v) { return new ImpactoDriver(TipoImpacto.PORCENTAJE, bd(v)); }
    private static ImpactoDriver monto(String v) { return new ImpactoDriver(TipoImpacto.MONTO, bd(v)); }

    @Test
    void sinImpactoQuedaLaUtilidadDelCaso() {
        assertEquals(bd("100000.00"), CalculadoraResultadoCaso.utilidadConDecision(caso(), null));
    }

    @Test
    void aplicaPorcentajeYMonto() {
        ImpactoOpcion i = new ImpactoOpcion(pct("15"), null, null, null, monto("30000"), null);
        assertEquals(bd("190000.00"), CalculadoraResultadoCaso.utilidadConDecision(caso(), i));
    }

    @Test
    void unRubroNuncaQuedaNegativo() {
        ImpactoOpcion i = new ImpactoOpcion(null, monto("-600000"), null, null, null, null);
        assertEquals(bd("600000.00"), CalculadoraResultadoCaso.utilidadConDecision(caso(), i));
    }

    @Test
    void penalizaConElMaximoDelRango() {
        assertEquals(bd("92000.00"), CalculadoraResultadoCaso.utilidadPenalizada(caso()));
        assertEquals(bd("8"), CalculadoraResultadoCaso.porcentajePenalizacion(caso()));
    }

    @Test
    void conPerdidaLaPenalizacionLaHaceMasGrande() {
        Caso c = caso();
        c.setUtilidadNeta(bd("-50000"));
        assertEquals(bd("-54000.00"), CalculadoraResultadoCaso.utilidadPenalizada(c));
    }

    @Test
    void casoSinPartidasUsaSuUtilidadNeta() {
        Caso c = new Caso();
        c.setUtilidadNeta(bd("70000"));
        c.setPenalizacionMax(bd("10"));
        ImpactoOpcion i = new ImpactoOpcion(pct("50"), null, null, null, null, null);
        assertEquals(bd("70000.00"), CalculadoraResultadoCaso.utilidadConDecision(c, i));
        assertEquals(bd("63000.00"), CalculadoraResultadoCaso.utilidadPenalizada(c));
    }
}

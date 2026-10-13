package com.estratego.domain.service;

import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.ImpactoDriver;
import com.estratego.domain.model.caso.ImpactoOpcion;
import com.estratego.domain.model.caso.TipoImpacto;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MotorArrastreTest {

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    private static void igual(String esperado, BigDecimal real) {
        assertEquals(0, bd(esperado).compareTo(real), "esperado " + esperado + " pero fue " + real);
    }

    /**
     * Balance del docente: activo 1.000.000 = pasivo 400.000 + capital 400.000 + retenidas 100.000
     * + utilidad 100.000 (ventas 800.000 − 500.000 − 80.000 − 60.000 − 20.000 − 40.000).
     */
    private static Caso primerCaso() {
        EstadoFinanciero ef = new EstadoFinanciero(
                bd("200000"), bd("150000"), bd("250000"), bd("350000"), bd("50000"),
                bd("150000"), bd("100000"), bd("150000"),
                bd("400000"), bd("100000"),
                bd("800000"), bd("500000"), bd("80000"), bd("60000"), bd("20000"), bd("40000"),
                bd("120000"), bd("-80000"), bd("-20000"));
        Caso c = new Caso();
        c.setId(1L);
        c.setFinanciero(ef);
        c.setUtilidadNeta(ef.utilidadNeta());
        c.setPenalizacionMin(bd("2"));
        c.setPenalizacionMax(bd("8"));
        return c;
    }

    /** Escenario del caso 2 (solo estado de resultados): utilidad 120.000. */
    private static Caso segundoCaso() {
        EstadoFinanciero ef = new EstadoFinanciero();
        ef.setVentasNetas(bd("900000"));
        ef.setCostoVentas(bd("550000"));
        ef.setGastosAdministracion(bd("90000"));
        ef.setGastosVentas(bd("70000"));
        ef.setGastosFinancieros(bd("20000"));
        ef.setImpuestoRenta(bd("50000"));
        Caso c = new Caso();
        c.setId(2L);
        c.setFinanciero(ef);
        c.setUtilidadNeta(ef.utilidadNeta());
        c.setPenalizacionMax(bd("10"));
        return c;
    }

    private static ImpactoOpcion ventasMas10() {
        return new ImpactoOpcion(new ImpactoDriver(TipoImpacto.PORCENTAJE, bd("10")), null, null, null, null, null);
    }

    @Test
    void elInicialDelPrimerCasoEsUnaCopiaExactaDelBalanceDelDocente() {
        Caso primero = primerCaso();

        EstadoFinanciero inicial = MotorArrastre.inicialDocente(primero, primero);

        assertNotSame(primero.getFinanciero(), inicial);
        assertEquals(primero.getFinanciero(), inicial);
        igual("120000", inicial.getFlujoOperativo());
        igual("-80000", inicial.getFlujoInversion());
        assertTrue(inicial.cuadra());
    }

    @Test
    void conDecisionLaDiferenciaDeUtilidadEntraAEfectivo() {
        Caso primero = primerCaso();
        EstadoFinanciero inicial = MotorArrastre.inicialDocente(primero, primero);

        EstadoFinanciero fin = MotorArrastre.finalConDecision(inicial, ventasMas10());

        igual("880000", fin.getVentasNetas());
        igual("180000", fin.utilidadNeta());
        igual("280000", fin.getEfectivo());          // 200.000 + (180.000 − 100.000)
        igual("100000", fin.getUtilidadesRetenidas()); // pasa a retenidas al abrir el caso siguiente
        igual("180000", fin.getFlujoOperativo());
        igual("0", fin.getFlujoInversion());
        igual("0", fin.getFlujoFinanciacion());
        igual("150000", fin.getCuentasPorCobrar());
        assertTrue(fin.cuadra());
        // Misma utilidad que usa la clasificación
        igual(CalculadoraResultadoCaso.utilidadConDecision(primero, ventasMas10()).toPlainString(), fin.utilidadNeta());
    }

    @Test
    void sinImpactoElEstadoDeResultadosNoCambia() {
        Caso primero = primerCaso();
        EstadoFinanciero inicial = MotorArrastre.inicialDocente(primero, primero);

        EstadoFinanciero fin = MotorArrastre.finalConDecision(inicial, null);

        igual("100000", fin.utilidadNeta());
        igual("200000", fin.getEfectivo());
        igual("100000", fin.getFlujoOperativo());
        assertTrue(fin.cuadra());
    }

    @Test
    void laPenalizacionSeRegistraComoGastoYReduceEfectivo() {
        Caso primero = primerCaso();
        EstadoFinanciero inicial = MotorArrastre.inicialDocente(primero, primero);

        EstadoFinanciero fin = MotorArrastre.finalPenalizado(inicial, bd("8"));

        igual("88000", fin.getGastosAdministracion()); // 80.000 + 8% de 100.000
        igual("92000", fin.utilidadNeta());
        igual("192000", fin.getEfectivo());
        assertTrue(fin.cuadra());
        igual(CalculadoraResultadoCaso.utilidadPenalizada(primero).toPlainString(), fin.utilidadNeta());
    }

    @Test
    void elInicialHeredadoPasaLaUtilidadARetenidasYAbreElNuevoEscenario() {
        Caso primero = primerCaso();
        EstadoFinanciero fin1 = MotorArrastre.finalConDecision(
                MotorArrastre.inicialDocente(primero, primero), ventasMas10());

        EstadoFinanciero ini2 = MotorArrastre.inicialHeredado(fin1, segundoCaso());

        igual("280000", ini2.getUtilidadesRetenidas()); // 100.000 + 180.000
        igual("400000", ini2.getEfectivo());            // 280.000 + utilidad proyectada 120.000
        igual("900000", ini2.getVentasNetas());         // escenario fresco, sin impacto acumulado
        igual("120000", ini2.utilidadNeta());
        igual("120000", ini2.getFlujoOperativo());
        igual("150000", ini2.getCuentasPorCobrar());
        igual("400000", ini2.getCapitalSocial());
        assertTrue(ini2.cuadra());
    }

    @Test
    void unaCadenaDeTresCasosCuadraEnCadaPaso() {
        Caso primero = primerCaso();
        Caso segundo = segundoCaso();

        EstadoFinanciero ini1 = MotorArrastre.inicialDocente(primero, primero);
        EstadoFinanciero fin1 = MotorArrastre.finalConDecision(ini1, ventasMas10());   // 180.000
        EstadoFinanciero ini2 = MotorArrastre.inicialHeredado(fin1, segundo);
        EstadoFinanciero fin2 = MotorArrastre.finalPenalizado(ini2, bd("10"));         // 120.000 − 12.000
        EstadoFinanciero ini3 = MotorArrastre.inicialHeredado(fin2, segundo);
        EstadoFinanciero fin3 = MotorArrastre.finalConDecision(ini3, ventasMas10());   // 210.000

        for (EstadoFinanciero e : new EstadoFinanciero[]{ini1, fin1, ini2, fin2, ini3, fin3}) {
            assertTrue(e.cuadra(), "no cuadra: " + e);
        }
        igual("108000", fin2.utilidadNeta());
        igual("388000", ini3.getUtilidadesRetenidas()); // 100.000 + 180.000 + 108.000
        igual("210000", fin3.utilidadNeta());
        // Todo lo ganado terminó en efectivo: 200.000 − 100.000 (proyección del docente) + 180.000 + 108.000 + 210.000
        igual("598000", fin3.getEfectivo());
    }

    @Test
    void lasPerdidasPuedenDejarElEfectivoNegativoYElBalanceSigueCuadrando() {
        Caso primero = primerCaso();
        ImpactoOpcion costoMas500mil = new ImpactoOpcion(
                null, new ImpactoDriver(TipoImpacto.MONTO, bd("500000")), null, null, null, null);

        EstadoFinanciero fin = MotorArrastre.finalConDecision(
                MotorArrastre.inicialDocente(primero, primero), costoMas500mil);

        igual("-400000", fin.utilidadNeta());
        igual("-300000", fin.getEfectivo()); // 200.000 − 500.000
        assertTrue(fin.cuadra());

        EstadoFinanciero ini2 = MotorArrastre.inicialHeredado(fin, segundoCaso());
        igual("-300000", ini2.getUtilidadesRetenidas()); // 100.000 − 400.000
        assertTrue(ini2.cuadra());
    }

    @Test
    void unaEmpresaQueEntraTardeEmpiezaConElBalanceDelDocenteYElEscenarioDeSuCaso() {
        Caso primero = primerCaso();

        EstadoFinanciero ini = MotorArrastre.inicialDocente(primero, segundoCaso());

        igual("100000", ini.getUtilidadesRetenidas());
        igual("220000", ini.getEfectivo()); // 200.000 − 100.000 (escenario 1) + 120.000 (escenario 2)
        igual("900000", ini.getVentasNetas());
        igual("120000", ini.getFlujoOperativo());
        igual("0", ini.getFlujoInversion());
        assertTrue(ini.cuadra());
    }
}

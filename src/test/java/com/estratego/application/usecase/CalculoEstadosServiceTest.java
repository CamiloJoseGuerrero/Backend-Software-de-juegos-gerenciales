package com.estratego.application.usecase;

import com.estratego.domain.model.caso.*;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.model.financiero.OrigenEstado;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculoEstadosServiceTest {

    private static final Long SIM = 10L;
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 20, 8, 0);

    @Mock private SimulacionRepository simulacionRepository;
    @Mock private CasoRepository casoRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private DecisionCasoRepository decisionCasoRepository;

    private final EstadosEnMemoria estados = new EstadosEnMemoria();
    private Simulacion simulacion;
    private Caso caso1;
    private Caso caso2;
    private final List<Empresa> empresas = new ArrayList<>();

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    private static void igual(String esperado, BigDecimal real) {
        assertEquals(0, bd(esperado).compareTo(real), "esperado " + esperado + " pero fue " + real);
    }

    /** Repositorio en memoria con la misma semántica que el real (upsert por empresa, caso y momento). */
    static class EstadosEnMemoria implements EstadoFinancieroEmpresaRepository {
        final List<EstadoFinancieroEmpresa> filas = new ArrayList<>();
        long secuencia = 1;

        @Override
        public EstadoFinancieroEmpresa guardar(EstadoFinancieroEmpresa e) {
            buscar(e.getIdEmpresa(), e.getIdCaso(), e.getMomento()).ifPresent(filas::remove);
            if (e.getId() == null) e.setId(secuencia++);
            filas.add(e);
            return e;
        }

        @Override
        public Optional<EstadoFinancieroEmpresa> buscar(Long idEmpresa, Long idCaso, MomentoEstado momento) {
            return filas.stream().filter(f -> f.getIdEmpresa().equals(idEmpresa) && f.getIdCaso().equals(idCaso)
                    && f.getMomento() == momento).findFirst();
        }

        @Override
        public List<EstadoFinancieroEmpresa> listarPorCaso(Long idCaso) {
            return filas.stream().filter(f -> f.getIdCaso().equals(idCaso)).toList();
        }

        @Override
        public List<EstadoFinancieroEmpresa> listarPorEmpresa(Long idEmpresa) {
            return filas.stream().filter(f -> f.getIdEmpresa().equals(idEmpresa)).toList();
        }

        @Override
        public void eliminarPorCaso(Long idCaso) {
            filas.removeIf(f -> f.getIdCaso().equals(idCaso));
        }

        EstadoFinancieroEmpresa de(Long idEmpresa, Long idCaso, MomentoEstado momento) {
            return buscar(idEmpresa, idCaso, momento).orElseThrow(() ->
                    new AssertionError("no hay " + momento + " de la empresa " + idEmpresa + " en el caso " + idCaso));
        }
    }

    @BeforeEach
    void setUp() {
        simulacion = new Simulacion(SIM, 99L, "Simulación", T0.toLocalDate(), T0.toLocalDate().plusDays(30),
                EstadoSimulacion.EN_CURSO);
        lenient().when(simulacionRepository.findById(SIM)).thenAnswer(inv -> Optional.of(simulacion));

        // Caso 1: balance del docente (activo 1.000.000; utilidad 100.000), penalización máx. 8 %
        caso1 = new Caso();
        caso1.setId(1L);
        caso1.setIdSimulacion(SIM);
        caso1.setNombreEmpresa("Caso 1");
        caso1.setFinanciero(new EstadoFinanciero(
                bd("200000"), bd("150000"), bd("250000"), bd("350000"), bd("50000"),
                bd("150000"), bd("100000"), bd("150000"),
                bd("400000"), bd("100000"),
                bd("800000"), bd("500000"), bd("80000"), bd("60000"), bd("20000"), bd("40000"),
                bd("120000"), bd("-80000"), bd("-20000")));
        caso1.setUtilidadNeta(bd("100000"));
        caso1.setPenalizacionMax(bd("8"));
        caso1.setFechaInicio(T0);
        caso1.setFechaFin(T0.plusHours(2));
        caso1.setOpciones(new ArrayList<>(List.of(
                new OpcionCaso(11L, 1, "Ampliar", "Vende más",
                        new ImpactoOpcion(new ImpactoDriver(TipoImpacto.PORCENTAJE, bd("10")), null, null, null, null, null)),
                new OpcionCaso(12L, 2, "Esperar", "Nada cambia", null))));

        // Caso 2: solo estado de resultados (utilidad 120.000)
        EstadoFinanciero er2 = new EstadoFinanciero();
        er2.setVentasNetas(bd("900000"));
        er2.setCostoVentas(bd("550000"));
        er2.setGastosAdministracion(bd("90000"));
        er2.setGastosVentas(bd("70000"));
        er2.setGastosFinancieros(bd("20000"));
        er2.setImpuestoRenta(bd("50000"));
        caso2 = new Caso();
        caso2.setId(2L);
        caso2.setIdSimulacion(SIM);
        caso2.setNombreEmpresa("Caso 2");
        caso2.setFinanciero(er2);
        caso2.setUtilidadNeta(bd("120000"));
        caso2.setPenalizacionMax(bd("10"));
        caso2.setFechaInicio(T0.plusDays(1));
        caso2.setFechaFin(T0.plusDays(1).plusHours(2));
        caso2.setOpciones(new ArrayList<>(List.of(new OpcionCaso(21L, 1, "Única", "—", null))));

        lenient().when(casoRepository.findByIdSimulacion(SIM)).thenReturn(List.of(caso2, caso1));
        empresas.add(new Empresa(100L, SIM, "EMP-001", "Alfa", null, TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA));
        empresas.add(new Empresa(200L, SIM, "EMP-002", "Beta", null, TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA));
        lenient().when(empresaRepository.findByIdSimulacion(SIM)).thenAnswer(inv -> List.copyOf(empresas));
        lenient().when(decisionCasoRepository.findByIdCaso(anyLong())).thenReturn(List.of());
    }

    private CalculoEstadosService servicio(LocalDateTime ahora) {
        Clock reloj = Clock.fixed(ahora.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        return new CalculoEstadosService(simulacionRepository, casoRepository, empresaRepository,
                decisionCasoRepository, estados, reloj);
    }

    private void activar(Caso c, LocalDateTime cuando, boolean activoAhora) {
        c.setActivadoEn(cuando);
        c.setEstado(activoAhora ? EstadoCaso.ACTIVO : EstadoCaso.BORRADOR);
    }

    private void decidio(Long idCaso, Long idEmpresa, Long idOpcion) {
        when(decisionCasoRepository.findByIdCaso(idCaso)).thenReturn(List.of(
                new DecisionCaso(1L, idCaso, idEmpresa, idOpcion, 7L, T0.plusMinutes(30))));
    }

    @Test
    void conElPrimerCasoAbiertoSiembraLosInicialesDelDocenteUnaSolaVez() {
        activar(caso1, T0, true);

        var r = servicio(T0.plusHours(1)).procesar(SIM);

        assertEquals(new CalculoEstadosService.Resultado(2, 0), r);
        EstadoFinancieroEmpresa ini = estados.de(100L, 1L, MomentoEstado.INICIAL);
        assertEquals(OrigenEstado.DOCENTE, ini.getOrigen());
        assertEquals(caso1.getFinanciero(), ini.getEstado());
        assertEquals(CalculoEstadosService.Resultado.NADA, servicio(T0.plusHours(1)).procesar(SIM));
        assertEquals(2, estados.filas.size());
    }

    @Test
    void alPasarLaFechaDeFinCierraConLaDecisionOConLaPenalizacion() {
        activar(caso1, T0, true);
        decidio(1L, 100L, 11L);

        var r = servicio(T0.plusHours(3)).procesar(SIM);

        assertEquals(new CalculoEstadosService.Resultado(2, 2), r);
        EstadoFinancieroEmpresa alfa = estados.de(100L, 1L, MomentoEstado.FINAL);
        assertEquals(OrigenEstado.DECISION, alfa.getOrigen());
        assertEquals(11L, alfa.getIdOpcion());
        igual("180000", alfa.getEstado().utilidadNeta());
        EstadoFinancieroEmpresa beta = estados.de(200L, 1L, MomentoEstado.FINAL);
        assertEquals(OrigenEstado.PENALIZACION, beta.getOrigen());
        assertNull(beta.getIdOpcion());
        igual("92000", beta.getEstado().utilidadNeta());
        assertTrue(alfa.getEstado().cuadra() && beta.getEstado().cuadra());
    }

    @Test
    void alActivarElSiguienteSeCierraElAnteriorYSeHereda() {
        activar(caso1, T0, false);
        activar(caso2, T0.plusHours(1), true); // antes de que termine el caso 1
        decidio(1L, 100L, 11L);

        servicio(T0.plusHours(1)).procesar(SIM);

        EstadoFinancieroEmpresa ini2 = estados.de(100L, 2L, MomentoEstado.INICIAL);
        assertEquals(OrigenEstado.HEREDADO, ini2.getOrigen());
        igual("280000", ini2.getEstado().getUtilidadesRetenidas()); // 100.000 + 180.000
        igual("120000", ini2.getEstado().utilidadNeta());
        assertTrue(ini2.getEstado().cuadra());
        igual("192000", estados.de(200L, 2L, MomentoEstado.INICIAL).getEstado().getUtilidadesRetenidas()); // 100.000 + 92.000
        assertTrue(estados.buscar(100L, 2L, MomentoEstado.FINAL).isEmpty());
    }

    @Test
    void unaEmpresaQueEntraTardeEmpiezaConElBalanceDelDocenteEnElCasoAbierto() {
        activar(caso1, T0, true);
        servicio(T0.plusHours(1)).procesar(SIM);
        activar(caso1, T0, false);
        activar(caso2, T0.plusDays(1), true);
        empresas.add(new Empresa(300L, SIM, "EMP-003", "Tardía", null, TipoJugador.MONOUSUARIO, EstadoEmpresa.ACTIVA));

        servicio(T0.plusDays(1).plusHours(1)).procesar(SIM);

        assertTrue(estados.buscar(300L, 1L, MomentoEstado.INICIAL).isEmpty());
        assertTrue(estados.buscar(300L, 1L, MomentoEstado.FINAL).isEmpty());
        EstadoFinancieroEmpresa ini = estados.de(300L, 2L, MomentoEstado.INICIAL);
        assertEquals(OrigenEstado.DOCENTE, ini.getOrigen());
        igual("220000", ini.getEstado().getEfectivo()); // 200.000 − 100.000 + 120.000
        assertEquals(OrigenEstado.HEREDADO, estados.de(100L, 2L, MomentoEstado.INICIAL).getOrigen());
    }

    @Test
    void alFinalizarLaSimulacionSeCierraElCasoAbiertoAunqueNoHayaPasadoSuFecha() {
        activar(caso1, T0, true);
        simulacion.setEstado(EstadoSimulacion.FINALIZADA);

        var r = servicio(T0.plusHours(1)).procesar(SIM);

        assertEquals(new CalculoEstadosService.Resultado(2, 2), r);
        assertEquals(OrigenEstado.PENALIZACION, estados.de(100L, 1L, MomentoEstado.FINAL).getOrigen());
    }

    @Test
    void sinSimulacionEnCursoNoCalculaNada() {
        activar(caso1, T0, true);
        simulacion.setEstado(EstadoSimulacion.PROGRAMADA);

        assertEquals(CalculoEstadosService.Resultado.NADA, servicio(T0.plusHours(3)).procesar(SIM));
        assertTrue(estados.filas.isEmpty());
    }

    @Test
    void siElPrimerCasoNoTieneBalanceCompletoNoCalculaNada() {
        activar(caso1, T0, true);
        caso1.getFinanciero().setEfectivo(null);

        assertEquals(CalculoEstadosService.Resultado.NADA, servicio(T0.plusHours(3)).procesar(SIM));
        assertTrue(estados.filas.isEmpty());
    }

    @Test
    void losCasosQueNuncaSeActivaronNoCuentan() {
        var r = servicio(T0.plusDays(5)).procesar(SIM);

        assertEquals(CalculoEstadosService.Resultado.NADA, r);
        verifyNoInteractions(decisionCasoRepository);
    }
}

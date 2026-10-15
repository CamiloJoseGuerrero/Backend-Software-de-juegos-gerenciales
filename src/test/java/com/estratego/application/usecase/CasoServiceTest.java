package com.estratego.application.usecase;

import com.estratego.domain.model.caso.TipoImpacto;

import com.estratego.application.dto.docente.CasoRequest;
import com.estratego.application.dto.docente.FinancieroCaso;
import com.estratego.application.dto.docente.ImpactoDriverRequest;
import com.estratego.application.dto.docente.ImpactoOpcionRequest;
import com.estratego.application.dto.docente.OpcionCasoRequest;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.EstadoCaso;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.CasoRepository;
import com.estratego.domain.repository.DecisionCasoRepository;
import com.estratego.domain.repository.EmpresaRepository;
import com.estratego.domain.repository.IntegranteRepository;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.integrante.Departamento;
import com.estratego.domain.model.integrante.Integrante;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CasoServiceTest {

    private static final String CORREO_DOCENTE = "docente@test.com";
    private static final Long DOCENTE_ID = 99L;
    private static final Long SIMULACION_ID = 10L;
    private static final LocalDate INICIO_SIM = LocalDate.now().plusDays(1);

    @Mock private CasoRepository casoRepository;
    @Mock private SimulacionRepository simulacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private DecisionCasoRepository decisionCasoRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;
    @Mock private IntegranteRepository integranteRepository;

    @InjectMocks
    private CasoService casoService;

    private Simulacion simulacion;

    @BeforeEach
    void setUp() {
        Usuario docente = new Usuario(DOCENTE_ID, "Docente", CORREO_DOCENTE, "DOC", "docente", "hash", Rol.DOCENTE);
        lenient().when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
        simulacion = new Simulacion(SIMULACION_ID, DOCENTE_ID, "Simulación", INICIO_SIM,
                INICIO_SIM.plusDays(30), EstadoSimulacion.BORRADOR);
    }

    /**
     * Activo 1.000.000 = Pasivo 400.000 + Patrimonio 600.000
     * (capital 400.000 + retenidas 100.000 + utilidad neta 100.000).
     */
    private FinancieroCaso financieroValido() {
        FinancieroCaso f = new FinancieroCaso();
        f.setEfectivo(new BigDecimal("200000"));
        f.setCuentasPorCobrar(new BigDecimal("150000"));
        f.setInventarios(new BigDecimal("250000"));
        f.setPropiedadPlantaEquipo(new BigDecimal("350000"));
        f.setActivosIntangibles(new BigDecimal("50000"));
        f.setCuentasPorPagar(new BigDecimal("150000"));
        f.setObligacionesFinancierasCortoPlazo(new BigDecimal("100000"));
        f.setObligacionesFinancierasLargoPlazo(new BigDecimal("150000"));
        f.setCapitalSocial(new BigDecimal("400000"));
        f.setUtilidadesRetenidas(new BigDecimal("100000"));
        f.setVentasNetas(new BigDecimal("800000"));
        f.setCostoVentas(new BigDecimal("500000"));
        f.setGastosAdministracion(new BigDecimal("80000"));
        f.setGastosVentas(new BigDecimal("60000"));
        f.setGastosFinancieros(new BigDecimal("20000"));
        f.setImpuestoRenta(new BigDecimal("40000"));
        f.setFlujoOperativo(new BigDecimal("120000"));
        f.setFlujoInversion(new BigDecimal("-80000"));
        f.setFlujoFinanciacion(new BigDecimal("-20000"));
        return f;
    }

    private void guardarDevuelveConId() {
        when(casoRepository.save(any(Caso.class))).thenAnswer(inv -> {
            Caso c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
    }

    private CasoRequest requestValido() {
        LocalDateTime inicio = INICIO_SIM.atTime(8, 0);
        return new CasoRequest(
                null, "TextilAndes S.A.", "Manufactura", "Vestir a Colombia", "Líder regional en 2030",
                financieroValido(),
                new BigDecimal("2"), new BigDecimal("8"),
                inicio.minusHours(2), inicio, inicio.plusHours(2),
                null,
                new ArrayList<>(List.of(
                        new OpcionCasoRequest(" Ampliar planta ", "Sube la capacidad"),
                        new OpcionCasoRequest("Reducir costos", "Mejora el margen"))));
    }

    private void simulacionValida() {
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
    }

    @Test
    void crearCasoGuardaOpcionesEnOrden() {
        simulacionValida();
        when(casoRepository.save(any(Caso.class))).thenAnswer(inv -> {
            Caso c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        var r = casoService.crear(SIMULACION_ID, requestValido(), CORREO_DOCENTE);

        assertEquals(1L, r.getId());
        assertEquals(SIMULACION_ID, r.getIdSimulacion());
        assertEquals(2, r.getOpciones().size());
        assertEquals(1, r.getOpciones().get(0).getOrden());
        assertEquals("Ampliar planta", r.getOpciones().get(0).getOpcion());
        assertEquals(2, r.getOpciones().get(1).getOrden());
        assertEquals(EstadoCaso.BORRADOR, r.getEstado());
        assertEquals(com.estratego.domain.model.caso.AsignacionEquipos.MANUAL, r.getAsignacionEquipos());
        assertEquals(0, new BigDecimal("600000").compareTo(r.getFinanciero().getPatrimonio()));
        assertEquals(0, new BigDecimal("1000000").compareTo(r.getFinanciero().getActivoTotal()));
        assertEquals(0, new BigDecimal("400000").compareTo(r.getFinanciero().getPasivoTotal()));
        assertEquals(0, new BigDecimal("100000").compareTo(r.getFinanciero().getUtilidadNeta()));
        assertEquals(new BigDecimal("800000"), r.getFinanciero().getVentasNetas());
        assertEquals(new BigDecimal("-80000"), r.getFinanciero().getFlujoInversion());
    }

    @Test
    void losTotalesLosCalculaElBackendAunqueElClienteMandeOtros() {
        simulacionValida();
        guardarDevuelveConId();
        CasoRequest req = requestValido();
        req.getFinanciero().setActivoTotal(new BigDecimal("1"));
        req.getFinanciero().setUtilidadNeta(new BigDecimal("999"));

        var r = casoService.crear(SIMULACION_ID, req, CORREO_DOCENTE);

        assertEquals(0, new BigDecimal("1000000").compareTo(r.getFinanciero().getActivoTotal()));
        assertEquals(0, new BigDecimal("100000").compareTo(r.getFinanciero().getUtilidadNeta()));
    }

    @Test
    void rechazaBalanceQueNoCuadra() {
        simulacionValida();
        CasoRequest req = requestValido();
        req.getFinanciero().setEfectivo(new BigDecimal("250000")); // activo sube 50.000

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> casoService.crear(SIMULACION_ID, req, CORREO_DOCENTE));
        assertEquals("El balance no cuadra: activo total 1050000, pasivo + patrimonio 1000000", ex.getMessage());
        verify(casoRepository, never()).save(any());
    }

    @Test
    void guardaYDevuelveElImpactoDeLasOpciones() {
        simulacionValida();
        guardarDevuelveConId();
        CasoRequest req = requestValido();
        ImpactoOpcionRequest impacto = new ImpactoOpcionRequest();
        impacto.setVentasNetas(new ImpactoDriverRequest(TipoImpacto.PORCENTAJE, new BigDecimal("15")));
        impacto.setCostoVentas(new ImpactoDriverRequest(TipoImpacto.MONTO, new BigDecimal("-20000")));
        req.getOpciones().get(0).setImpacto(impacto);

        var r = casoService.crear(SIMULACION_ID, req, CORREO_DOCENTE);

        var i = r.getOpciones().get(0).getImpacto();
        assertEquals(TipoImpacto.PORCENTAJE, i.ventasNetas().tipo());
        assertEquals(new BigDecimal("15"), i.ventasNetas().valor());
        assertEquals(new BigDecimal("-20000"), i.costoVentas().valor());
        assertNull(i.gastosVentas());
        assertNull(r.getOpciones().get(1).getImpacto());
    }

    @Test
    void rechazaPenalizacionMinimaMayorQueMaxima() {
        simulacionValida();
        CasoRequest req = requestValido();
        req.setPenalizacionMin(new BigDecimal("9"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> casoService.crear(SIMULACION_ID, req, CORREO_DOCENTE));
        assertEquals("La penalización mínima no puede ser mayor que la máxima", ex.getMessage());
        verify(casoRepository, never()).save(any());
    }

    @Test
    void rechazaVisualizacionPosteriorAlInicio() {
        simulacionValida();
        CasoRequest req = requestValido();
        req.setFechaVisualizacion(req.getFechaInicioPartida().plusMinutes(1));

        assertThrows(IllegalArgumentException.class,
                () -> casoService.crear(SIMULACION_ID, req, CORREO_DOCENTE));
    }

    @Test
    void rechazaFinIgualOAnteriorAlInicio() {
        simulacionValida();
        CasoRequest req = requestValido();
        req.setFechaFinPartida(req.getFechaInicioPartida());

        assertThrows(IllegalArgumentException.class,
                () -> casoService.crear(SIMULACION_ID, req, CORREO_DOCENTE));
    }

    @Test
    void rechazaPartidaFueraDelRangoDeLaSimulacion() {
        simulacionValida();
        CasoRequest antes = requestValido();
        antes.setFechaVisualizacion(INICIO_SIM.minusDays(2).atStartOfDay());
        antes.setFechaInicioPartida(INICIO_SIM.minusDays(1).atTime(8, 0));
        antes.setFechaFinPartida(INICIO_SIM.minusDays(1).atTime(10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> casoService.crear(SIMULACION_ID, antes, CORREO_DOCENTE));

        CasoRequest despues = requestValido();
        despues.setFechaFinPartida(INICIO_SIM.plusDays(31).atTime(10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> casoService.crear(SIMULACION_ID, despues, CORREO_DOCENTE));
    }

    @Test
    void rechazaCambiosSiLaSimulacionEstaEnCurso() {
        simulacion.setEstado(EstadoSimulacion.EN_CURSO);
        simulacionValida();

        assertThrows(IllegalArgumentException.class,
                () -> casoService.crear(SIMULACION_ID, requestValido(), CORREO_DOCENTE));
    }

    @Test
    void rechazaSimulacionDeOtroDocente() {
        simulacion.setIdUsuarioCoordinador(1234L);
        simulacionValida();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> casoService.listarPorSimulacion(SIMULACION_ID, CORREO_DOCENTE));
        assertEquals("La simulación no pertenece a este docente", ex.getMessage());
    }

    @Test
    void actualizarReemplazaLasOpciones() {
        simulacionValida();
        Caso existente = new Caso();
        existente.setId(5L);
        existente.setIdSimulacion(SIMULACION_ID);
        when(casoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(casoRepository.save(any(Caso.class))).thenAnswer(inv -> inv.getArgument(0));

        CasoRequest req = requestValido();
        req.setOpciones(new ArrayList<>(List.of(new OpcionCasoRequest("Única", "Resultado único"))));

        var r = casoService.actualizar(5L, req, CORREO_DOCENTE);

        ArgumentCaptor<Caso> captor = ArgumentCaptor.forClass(Caso.class);
        verify(casoRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getOpciones().size());
        assertEquals("TextilAndes S.A.", r.getNombre());
    }

    @Test
    void eliminarCasoDeSimulacionEditable() {
        simulacionValida();
        Caso existente = new Caso();
        existente.setId(5L);
        existente.setIdSimulacion(SIMULACION_ID);
        when(casoRepository.findById(5L)).thenReturn(Optional.of(existente));

        casoService.eliminar(5L, CORREO_DOCENTE);

        verify(casoRepository).deleteById(5L);
    }

    @Test
    void obtenerCasoInexistenteFalla() {
        when(casoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> casoService.obtener(5L, CORREO_DOCENTE));
    }

    @Test
    void crearSinSimulacionFalla() {
        assertThrows(IllegalArgumentException.class,
                () -> casoService.crear(null, requestValido(), CORREO_DOCENTE));
    }

    @Test
    void activarDesactivaElAnteriorSinTocarOpciones() {
        simulacionValida();
        Caso existente = new Caso();
        existente.setId(5L);
        existente.setIdSimulacion(SIMULACION_ID);
        when(casoRepository.findById(5L)).thenReturn(Optional.of(existente));

        var r = casoService.activar(5L, CORREO_DOCENTE);

        assertEquals(EstadoCaso.ACTIVO, r.getEstado());
        var orden = inOrder(casoRepository);
        orden.verify(casoRepository).desactivarTodos(SIMULACION_ID);
        orden.verify(casoRepository).marcarActivo(5L);
        verify(casoRepository, never()).save(any());
        // D2: pide sembrar/cerrar los estados financieros de la simulación después del commit
        verify(eventPublisher).publishEvent(
                new com.estratego.application.event.EstadosFinancierosPendientesEvent(SIMULACION_ID));
    }

    @Test
    void noSeActivaSiUnaEmpresaConIntegrantesNoTieneLider() {
        simulacionValida();
        Caso existente = new Caso();
        existente.setId(5L);
        existente.setIdSimulacion(SIMULACION_ID);
        when(casoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(empresaRepository.findByIdSimulacion(SIMULACION_ID)).thenReturn(List.of(
                new Empresa(1L, SIMULACION_ID, "EMP-001", "Con líder", null, TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA),
                new Empresa(2L, SIMULACION_ID, "EMP-002", "Sin líder", null, TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA),
                new Empresa(3L, SIMULACION_ID, "EMP-003", "Vacía", null, TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA)));
        when(integranteRepository.findByIdEmpresa(1L)).thenReturn(List.of(
                new Integrante(1L, 1L, 20L, Departamento.COMERCIAL, true)));
        when(integranteRepository.findByIdEmpresa(2L)).thenReturn(List.of(
                new Integrante(2L, 2L, 21L, Departamento.COMERCIAL, false)));
        when(integranteRepository.findByIdEmpresa(3L)).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> casoService.activar(5L, CORREO_DOCENTE));

        assertEquals("No se puede activar el caso: estas empresas no tienen líder: EMP-002 (Sin líder)",
                ex.getMessage());
        verify(casoRepository, never()).marcarActivo(any());
    }

    @Test
    void elBalanceQueNoCuadraSeAtribuyeAlCampoFinanciero() {
        simulacionValida();
        CasoRequest req = requestValido();
        req.getFinanciero().setEfectivo(new BigDecimal("250000"));

        CampoInvalidoException ex = assertThrows(CampoInvalidoException.class,
                () -> casoService.crear(SIMULACION_ID, req, CORREO_DOCENTE));
        assertEquals("financiero", ex.getCampo());
    }

    @Test
    void noSePuedeEditarNiEliminarUnCasoConDecisiones() {
        simulacionValida();
        Caso existente = new Caso();
        existente.setId(5L);
        existente.setIdSimulacion(SIMULACION_ID);
        when(casoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(decisionCasoRepository.existsByIdCaso(5L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> casoService.actualizar(5L, requestValido(), CORREO_DOCENTE));
        assertThrows(IllegalArgumentException.class,
                () -> casoService.eliminar(5L, CORREO_DOCENTE));
        verify(casoRepository, never()).save(any());
        verify(casoRepository, never()).deleteById(any());
    }

    // ---- D2: solo el primer caso de la simulación define el balance ----

    /** Solo las 6 partidas del estado de resultados (utilidad 100.000); sin balance ni flujos. */
    private FinancieroCaso soloEstadoDeResultados() {
        FinancieroCaso f = financieroValido();
        f.setEfectivo(null);
        f.setCuentasPorCobrar(null);
        f.setInventarios(null);
        f.setPropiedadPlantaEquipo(null);
        f.setActivosIntangibles(null);
        f.setCuentasPorPagar(null);
        f.setObligacionesFinancierasCortoPlazo(null);
        f.setObligacionesFinancierasLargoPlazo(null);
        f.setCapitalSocial(null);
        f.setUtilidadesRetenidas(null);
        f.setFlujoOperativo(null);
        f.setFlujoInversion(null);
        f.setFlujoFinanciacion(null);
        return f;
    }

    private Caso casoExistente(Long id, String nombre, LocalDateTime inicio, boolean conBalance) {
        Caso c = new Caso();
        c.setId(id);
        c.setIdSimulacion(SIMULACION_ID);
        c.setNombreEmpresa(nombre);
        c.setFechaInicio(inicio);
        c.setFechaFin(inicio.plusHours(2));
        c.setFinanciero((conBalance ? financieroValido() : soloEstadoDeResultados()).aEstadoFinanciero());
        return c;
    }

    /** requestValido() con sus tres fechas corridas unos días. */
    private CasoRequest requestDesplazado(int dias) {
        CasoRequest r = requestValido();
        r.setFechaVisualizacion(r.getFechaVisualizacion().plusDays(dias));
        r.setFechaInicioPartida(r.getFechaInicioPartida().plusDays(dias));
        r.setFechaFinPartida(r.getFechaFinPartida().plusDays(dias));
        return r;
    }

    @Test
    void elPrimerCasoNuevoQuedaMarcadoComoPrimero() {
        simulacionValida();
        guardarDevuelveConId();

        var r = casoService.crear(SIMULACION_ID, requestValido(), CORREO_DOCENTE);

        assertTrue(r.isEsPrimerCaso());
    }

    @Test
    void elPrimerCasoSinLasDiecinuevePartidasFalla() {
        simulacionValida();
        CasoRequest req = requestValido();
        req.getFinanciero().setEfectivo(null);
        req.getFinanciero().setFlujoInversion(null);

        CampoInvalidoException ex = assertThrows(CampoInvalidoException.class,
                () -> casoService.crear(SIMULACION_ID, req, CORREO_DOCENTE));
        assertEquals("financiero", ex.getCampo());
        assertEquals("El primer caso de la simulación debe traer las 19 partidas; faltan: efectivo, flujoInversion",
                ex.getMessage());
        verify(casoRepository, never()).save(any());
    }

    @Test
    void unCasoSiguienteSoloNecesitaElEstadoDeResultados() {
        simulacionValida();
        guardarDevuelveConId();
        when(casoRepository.findByIdSimulacion(SIMULACION_ID)).thenReturn(List.of(
                casoExistente(3L, "Caso 1", INICIO_SIM.atTime(8, 0), true)));
        CasoRequest req = requestDesplazado(1);
        req.setFinanciero(soloEstadoDeResultados());

        var r = casoService.crear(SIMULACION_ID, req, CORREO_DOCENTE);

        assertFalse(r.isEsPrimerCaso());
        assertNull(r.getFinanciero().getActivoTotal());
        assertNull(r.getFinanciero().getPasivoTotal());
        assertNull(r.getFinanciero().getPatrimonio());
        assertNull(r.getFinanciero().getEfectivo());
        assertEquals(0, new BigDecimal("100000").compareTo(r.getFinanciero().getUtilidadNeta()));
    }

    @Test
    void unCasoNuevoConFechaAnteriorAlPrimeroDebeTraerElBalance() {
        simulacionValida();
        when(casoRepository.findByIdSimulacion(SIMULACION_ID)).thenReturn(List.of(
                casoExistente(3L, "Caso 1", INICIO_SIM.atTime(8, 0).plusDays(2), true)));
        CasoRequest req = requestValido();
        req.setFinanciero(soloEstadoDeResultados());

        CampoInvalidoException ex = assertThrows(CampoInvalidoException.class,
                () -> casoService.crear(SIMULACION_ID, req, CORREO_DOCENTE));
        assertTrue(ex.getMessage().startsWith("El primer caso de la simulación debe traer las 19 partidas; faltan: efectivo"));
        verify(casoRepository, never()).save(any());
    }

    @Test
    void moverElPrimerCasoDetrasDeUnoSinBalanceFalla() {
        simulacionValida();
        LocalDateTime t = INICIO_SIM.atTime(8, 0);
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoExistente(1L, "Caso A", t, true)));
        when(casoRepository.findByIdSimulacion(SIMULACION_ID)).thenReturn(List.of(
                casoExistente(1L, "Caso A", t, true),
                casoExistente(2L, "Caso B", t.plusDays(1), false)));

        CampoInvalidoException ex = assertThrows(CampoInvalidoException.class,
                () -> casoService.actualizar(1L, requestDesplazado(2), CORREO_DOCENTE));
        assertEquals("financiero", ex.getCampo());
        assertEquals("Al mover este caso, «Caso B» pasaría a ser el primero y no tiene balance inicial",
                ex.getMessage());
        verify(casoRepository, never()).save(any());
    }

    @Test
    void moverElPrimerCasoDetrasDeOtroConBalanceSePermite() {
        simulacionValida();
        LocalDateTime t = INICIO_SIM.atTime(8, 0);
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoExistente(1L, "Caso A", t, true)));
        when(casoRepository.findByIdSimulacion(SIMULACION_ID)).thenReturn(List.of(
                casoExistente(1L, "Caso A", t, true),
                casoExistente(2L, "Caso B", t.plusDays(1), true)));
        when(casoRepository.save(any(Caso.class))).thenAnswer(inv -> inv.getArgument(0));

        var r = casoService.actualizar(1L, requestDesplazado(2), CORREO_DOCENTE);

        assertFalse(r.isEsPrimerCaso());
    }

    @Test
    void eliminarElPrimerCasoDejandoPrimeroAUnoSinBalanceFalla() {
        simulacionValida();
        LocalDateTime t = INICIO_SIM.atTime(8, 0);
        when(casoRepository.findById(1L)).thenReturn(Optional.of(casoExistente(1L, "Caso A", t, true)));
        when(casoRepository.findByIdSimulacion(SIMULACION_ID)).thenReturn(List.of(
                casoExistente(1L, "Caso A", t, true),
                casoExistente(2L, "Caso B", t.plusDays(1), false)));

        CampoInvalidoException ex = assertThrows(CampoInvalidoException.class,
                () -> casoService.eliminar(1L, CORREO_DOCENTE));
        assertEquals("Al eliminar este caso, «Caso B» pasaría a ser el primero y no tiene balance inicial",
                ex.getMessage());
        verify(casoRepository, never()).deleteById(any());
    }

    @Test
    void listarMarcaSoloElPrimerCasoYEnEmpateElCreadoAntes() {
        simulacionValida();
        LocalDateTime t = INICIO_SIM.atTime(8, 0);
        when(casoRepository.findByIdSimulacion(SIMULACION_ID)).thenReturn(List.of(
                casoExistente(5L, "Tarde", t.plusDays(1), false),
                casoExistente(4L, "Empate nuevo", t, false),
                casoExistente(3L, "Empate viejo", t, true)));

        var r = casoService.listarPorSimulacion(SIMULACION_ID, CORREO_DOCENTE);

        assertEquals(List.of(false, false, true), r.stream().map(c -> c.isEsPrimerCaso()).toList());
    }
}

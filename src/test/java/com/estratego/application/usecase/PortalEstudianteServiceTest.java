package com.estratego.application.usecase;

import com.estratego.domain.model.caso.AsignacionEquipos;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.DecisionCaso;
import com.estratego.domain.model.caso.EstadoCaso;
import com.estratego.application.dto.estudiante.DecisionRequest;
import com.estratego.domain.model.caso.OpcionCaso;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.integrante.Departamento;
import com.estratego.domain.model.integrante.Integrante;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortalEstudianteServiceTest {

    private static final String CORREO = "ana@test.com";
    private static final Long ESTUDIANTE_ID = 20L;
    private static final Long SIM_ID = 10L;
    private static final Long EMPRESA_ID = 5L;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private IntegranteRepository integranteRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private SimulacionRepository simulacionRepository;
    @Mock private CasoRepository casoRepository;
    @Mock private DecisionCasoRepository decisionCasoRepository;

    @InjectMocks
    private PortalEstudianteService service;

    private Simulacion simulacion;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        Usuario ana = new Usuario(ESTUDIANTE_ID, "Ana", CORREO, "123", "ana", "hash", Rol.ESTUDIANTE);
        lenient().when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(ana));
        simulacion = new Simulacion(SIM_ID, 99L, "Simulación", LocalDate.now(), LocalDate.now().plusDays(30),
                EstadoSimulacion.PROGRAMADA);
        empresa = new Empresa(EMPRESA_ID, SIM_ID, "EMP-001", "TechStart", null,
                TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA);
    }

    private Caso caso(Long id, LocalDateTime visualizacion, LocalDateTime inicio, LocalDateTime fin) {
        return new Caso(id, SIM_ID, "TextilAndes", null, null, null,
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.TEN, visualizacion, inicio, fin,
                List.of(new OpcionCaso(1L, 1, "Ampliar", "Resultado secreto")),
                EstadoCaso.ACTIVO, AsignacionEquipos.MANUAL);
    }

    @Test
    void misSimulacionesDevuelveEmpresaYRol() {
        when(integranteRepository.findByIdUsuario(ESTUDIANTE_ID)).thenReturn(List.of(
                new Integrante(1L, EMPRESA_ID, ESTUDIANTE_ID, Departamento.COMERCIAL, true)));
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
        when(simulacionRepository.findById(SIM_ID)).thenReturn(Optional.of(simulacion));

        var r = service.misSimulaciones(CORREO);

        assertEquals(1, r.size());
        assertEquals("TechStart", r.get(0).getNombreEmpresa());
        assertEquals(Departamento.COMERCIAL, r.get(0).getDepartamento());
        assertTrue(r.get(0).isEsLider());
    }

    @Test
    void miEmpresaRechazaSiNoEsIntegrante() {
        when(integranteRepository.findByIdEmpresaAndIdUsuario(EMPRESA_ID, ESTUDIANTE_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.miEmpresa(EMPRESA_ID, CORREO));
        assertEquals("No perteneces a esta empresa", ex.getMessage());
    }

    @Test
    void miEmpresaListaCompaneros() {
        Integrante yo = new Integrante(1L, EMPRESA_ID, ESTUDIANTE_ID, Departamento.COMERCIAL, false);
        Integrante otro = new Integrante(2L, EMPRESA_ID, 21L, Departamento.GERENCIA_GENERAL, true);
        when(integranteRepository.findByIdEmpresaAndIdUsuario(EMPRESA_ID, ESTUDIANTE_ID)).thenReturn(Optional.of(yo));
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
        when(integranteRepository.findByIdEmpresa(EMPRESA_ID)).thenReturn(List.of(yo, otro));
        when(usuarioRepository.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(
                new Usuario(ESTUDIANTE_ID, "Ana", CORREO, "123", "ana", "hash", Rol.ESTUDIANTE)));
        when(usuarioRepository.findById(21L)).thenReturn(Optional.of(
                new Usuario(21L, "Luis", "luis@test.com", "456", "luis", "hash", Rol.ESTUDIANTE)));

        var r = service.miEmpresa(EMPRESA_ID, CORREO);

        assertEquals(2, r.getIntegrantes().size());
        assertTrue(r.getIntegrantes().get(1).isEsLider());
    }

    @Test
    void casosFiltraPorVisualizacionYOcultaOpcionesAntesDelInicio() {
        LocalDateTime ahora = LocalDateTime.now();
        Caso visibleSinIniciar = caso(1L, ahora.minusHours(1), ahora.plusHours(1), ahora.plusHours(3));
        Caso iniciado = caso(2L, ahora.minusHours(3), ahora.minusHours(1), ahora.plusHours(1));
        Caso oculto = caso(3L, ahora.plusDays(1), ahora.plusDays(2), ahora.plusDays(3));

        when(simulacionRepository.findById(SIM_ID)).thenReturn(Optional.of(simulacion));
        when(integranteRepository.existsEnSimulacion(SIM_ID, ESTUDIANTE_ID)).thenReturn(true);
        when(casoRepository.findByIdSimulacion(SIM_ID)).thenReturn(List.of(visibleSinIniciar, iniciado, oculto));

        var r = service.casosDeSimulacion(SIM_ID, CORREO);

        assertEquals(2, r.size());
        assertFalse(r.get(0).isPartidaIniciada());
        assertTrue(r.get(0).getOpciones().isEmpty());
        assertTrue(r.get(1).isPartidaIniciada());
        assertEquals("Ampliar", r.get(1).getOpciones().get(0).getOpcion());
    }

    @Test
    void casosDeSimulacionBorradorNoSeMuestran() {
        simulacion.setEstado(EstadoSimulacion.BORRADOR);
        LocalDateTime ahora = LocalDateTime.now();
        when(simulacionRepository.findById(SIM_ID)).thenReturn(Optional.of(simulacion));
        when(integranteRepository.existsEnSimulacion(SIM_ID, ESTUDIANTE_ID)).thenReturn(true);
        when(casoRepository.findByIdSimulacion(SIM_ID)).thenReturn(List.of(
                caso(1L, ahora.minusHours(1), ahora.plusHours(1), ahora.plusHours(2))));

        assertTrue(service.casosDeSimulacion(SIM_ID, CORREO).isEmpty());
    }

    @Test
    void casosRechazaSiNoParticipa() {
        when(simulacionRepository.findById(SIM_ID)).thenReturn(Optional.of(simulacion));
        when(integranteRepository.existsEnSimulacion(SIM_ID, ESTUDIANTE_ID)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.casosDeSimulacion(SIM_ID, CORREO));
        assertEquals("No participas en esta simulación", ex.getMessage());
        verify(casoRepository, never()).findByIdSimulacion(any());
    }

    @Test
    void casoAunNoVisibleFalla() {
        LocalDateTime ahora = LocalDateTime.now();
        when(casoRepository.findById(3L)).thenReturn(Optional.of(
                caso(3L, ahora.plusDays(1), ahora.plusDays(2), ahora.plusDays(3))));
        when(simulacionRepository.findById(SIM_ID)).thenReturn(Optional.of(simulacion));
        when(integranteRepository.existsEnSimulacion(SIM_ID, ESTUDIANTE_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.caso(3L, CORREO));
        assertEquals("Este caso todavía no está disponible", ex.getMessage());
    }

    // ---- caso actual y decisión ----

    private void participaComo(boolean lider) {
        when(integranteRepository.findByIdUsuario(ESTUDIANTE_ID)).thenReturn(List.of(
                new Integrante(1L, EMPRESA_ID, ESTUDIANTE_ID, Departamento.COMERCIAL, lider)));
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
        when(simulacionRepository.findById(SIM_ID)).thenReturn(Optional.of(simulacion));
    }

    private Caso casoEnPartida() {
        LocalDateTime ahora = LocalDateTime.now();
        return caso(7L, ahora.minusHours(2), ahora.minusHours(1), ahora.plusHours(1));
    }

    @Test
    void casoActualDelLiderPuedeDecidir() {
        participaComo(true);
        Caso c = casoEnPartida();
        when(casoRepository.findActivoBySimulacion(SIM_ID)).thenReturn(Optional.of(c));
        when(decisionCasoRepository.findByIdCasoAndIdEmpresa(7L, EMPRESA_ID)).thenReturn(Optional.empty());

        var r = service.casoActual(CORREO, null).orElseThrow();

        assertTrue(r.isPuedeDecidir());
        assertNull(r.getDecision());
        assertEquals(7L, r.getCaso().getId());
    }

    @Test
    void casoActualVacioSiNoHayCasoActivo() {
        participaComo(false);
        when(casoRepository.findActivoBySimulacion(SIM_ID)).thenReturn(Optional.empty());

        assertTrue(service.casoActual(CORREO, null).isEmpty());
    }

    @Test
    void casoActualMuestraLaDecisionConSuResultado() {
        participaComo(false);
        Caso c = casoEnPartida();
        when(casoRepository.findActivoBySimulacion(SIM_ID)).thenReturn(Optional.of(c));
        when(decisionCasoRepository.findByIdCasoAndIdEmpresa(7L, EMPRESA_ID)).thenReturn(Optional.of(
                new DecisionCaso(1L, 7L, EMPRESA_ID, 1L, 21L, LocalDateTime.now())));

        var r = service.casoActual(CORREO, null).orElseThrow();

        assertFalse(r.isPuedeDecidir());
        assertEquals("Resultado secreto", r.getDecision().getResultado());
    }

    // ---- caso actual con varias simulaciones ----

    private static final Long SIM2_ID = 11L;
    private static final Long EMPRESA2_ID = 6L;

    /**
     * Ana está en dos simulaciones: la 10 (más reciente, aparece primero) y la 11.
     * Es líder en ambas.
     */
    private void participaEnDosSimulaciones() {
        Simulacion sim2 = new Simulacion(SIM2_ID, 99L, "Otra simulación", LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(30), EstadoSimulacion.EN_CURSO);
        Empresa empresa2 = new Empresa(EMPRESA2_ID, SIM2_ID, "EMP-002", "AgroSur", null,
                TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA);
        when(integranteRepository.findByIdUsuario(ESTUDIANTE_ID)).thenReturn(List.of(
                new Integrante(1L, EMPRESA_ID, ESTUDIANTE_ID, Departamento.COMERCIAL, true),
                new Integrante(2L, EMPRESA2_ID, ESTUDIANTE_ID, Departamento.COMERCIAL, true)));
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
        when(empresaRepository.findById(EMPRESA2_ID)).thenReturn(Optional.of(empresa2));
        when(simulacionRepository.findById(SIM_ID)).thenReturn(Optional.of(simulacion));
        when(simulacionRepository.findById(SIM2_ID)).thenReturn(Optional.of(sim2));
    }

    private Caso casoDe(Long id, Long idSimulacion, LocalDateTime inicio, LocalDateTime fin) {
        Caso c = caso(id, inicio.minusDays(1), inicio, fin);
        c.setIdSimulacion(idSimulacion);
        return c;
    }

    @Test
    void casoActualSinFiltroPrefiereElQuePuedeDecidir() {
        participaEnDosSimulaciones();
        LocalDateTime ahora = LocalDateTime.now();
        // Sim 10 (aparece primero): caso ya terminado. Sim 11: caso en partida.
        Caso terminado = casoDe(30L, SIM_ID, ahora.minusDays(3), ahora.minusDays(2));
        Caso enPartida = casoDe(31L, SIM2_ID, ahora.minusHours(1), ahora.plusHours(1));
        when(casoRepository.findActivoBySimulacion(SIM_ID)).thenReturn(Optional.of(terminado));
        when(casoRepository.findActivoBySimulacion(SIM2_ID)).thenReturn(Optional.of(enPartida));
        when(decisionCasoRepository.findByIdCasoAndIdEmpresa(30L, EMPRESA_ID)).thenReturn(Optional.empty());
        when(decisionCasoRepository.findByIdCasoAndIdEmpresa(31L, EMPRESA2_ID)).thenReturn(Optional.empty());

        var r = service.casoActual(CORREO, null).orElseThrow();

        assertEquals(31L, r.getCaso().getId());
        assertEquals(SIM2_ID, r.getIdSimulacion());
        assertTrue(r.isPuedeDecidir());
    }

    @Test
    void casoActualSinFiltroPrefiereElProximoAlTerminado() {
        participaEnDosSimulaciones();
        LocalDateTime ahora = LocalDateTime.now();
        Caso terminado = casoDe(30L, SIM_ID, ahora.minusDays(3), ahora.minusDays(2));
        Caso proximo = casoDe(31L, SIM2_ID, ahora.plusHours(5), ahora.plusHours(8));
        when(casoRepository.findActivoBySimulacion(SIM_ID)).thenReturn(Optional.of(terminado));
        when(casoRepository.findActivoBySimulacion(SIM2_ID)).thenReturn(Optional.of(proximo));
        when(decisionCasoRepository.findByIdCasoAndIdEmpresa(30L, EMPRESA_ID)).thenReturn(Optional.empty());
        when(decisionCasoRepository.findByIdCasoAndIdEmpresa(31L, EMPRESA2_ID)).thenReturn(Optional.empty());

        var r = service.casoActual(CORREO, null).orElseThrow();

        assertEquals(31L, r.getCaso().getId());
        assertFalse(r.isPuedeDecidir());
    }

    @Test
    void casoActualConIdSimulacionRespetaElFiltro() {
        participaEnDosSimulaciones();
        LocalDateTime ahora = LocalDateTime.now();
        Caso terminado = casoDe(30L, SIM_ID, ahora.minusDays(3), ahora.minusDays(2));
        when(casoRepository.findActivoBySimulacion(SIM_ID)).thenReturn(Optional.of(terminado));
        when(decisionCasoRepository.findByIdCasoAndIdEmpresa(30L, EMPRESA_ID)).thenReturn(Optional.empty());

        var r = service.casoActual(CORREO, SIM_ID).orElseThrow();

        assertEquals(30L, r.getCaso().getId());
        verify(casoRepository, never()).findActivoBySimulacion(SIM2_ID);
    }

    @Test
    void liderDecideYQuedaGuardado() {
        participaComo(true);
        Caso c = casoEnPartida();
        when(casoRepository.findById(7L)).thenReturn(Optional.of(c));
        when(decisionCasoRepository.findByIdCasoAndIdEmpresa(7L, EMPRESA_ID)).thenReturn(Optional.empty());
        when(decisionCasoRepository.save(any(DecisionCaso.class))).thenAnswer(inv -> inv.getArgument(0));

        var r = service.decidir(new DecisionRequest(7L, 1L), CORREO);

        assertEquals("Ampliar", r.getOpcion());
        assertEquals("Resultado secreto", r.getResultado());
        verify(decisionCasoRepository).save(any(DecisionCaso.class));
    }

    @Test
    void quienNoEsLiderNoPuedeDecidir() {
        participaComo(false);
        when(casoRepository.findById(7L)).thenReturn(Optional.of(casoEnPartida()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.decidir(new DecisionRequest(7L, 1L), CORREO));
        assertEquals("Solo el líder de la empresa puede tomar la decisión", ex.getMessage());
        verify(decisionCasoRepository, never()).save(any());
    }

    @Test
    void laDecisionEsPermanente() {
        participaComo(true);
        when(casoRepository.findById(7L)).thenReturn(Optional.of(casoEnPartida()));
        when(decisionCasoRepository.findByIdCasoAndIdEmpresa(7L, EMPRESA_ID)).thenReturn(Optional.of(
                new DecisionCaso(1L, 7L, EMPRESA_ID, 1L, ESTUDIANTE_ID, LocalDateTime.now())));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.decidir(new DecisionRequest(7L, 1L), CORREO));
        assertEquals("Tu empresa ya tomó su decisión y no se puede cambiar", ex.getMessage());
    }

    @Test
    void noSeDecideAntesDelInicioNiConOpcionAjena() {
        participaComo(true);
        LocalDateTime ahora = LocalDateTime.now();
        when(casoRepository.findById(8L)).thenReturn(Optional.of(
                caso(8L, ahora.minusHours(1), ahora.plusHours(1), ahora.plusHours(2))));
        assertThrows(IllegalArgumentException.class,
                () -> service.decidir(new DecisionRequest(8L, 1L), CORREO));

        when(casoRepository.findById(7L)).thenReturn(Optional.of(casoEnPartida()));
        when(decisionCasoRepository.findByIdCasoAndIdEmpresa(7L, EMPRESA_ID)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.decidir(new DecisionRequest(7L, 999L), CORREO));
        assertEquals("La opción no pertenece a este caso", ex.getMessage());
    }
}

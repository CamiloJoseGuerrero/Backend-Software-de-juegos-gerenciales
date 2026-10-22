package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarSimulacionRequest;
import com.estratego.application.dto.docente.CrearSimulacionRequest;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulacionServiceTest {

    private static final String CORREO_COORDINADOR = "coordinador@test.com";
    private static final Long COORDINADOR_ID = 99L;

    @Mock
    private SimulacionRepository simulacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Mock private com.estratego.domain.repository.CasoRepository casoRepository;
    @Mock private com.estratego.domain.repository.EmpresaRepository empresaRepository;
    @Mock private com.estratego.domain.repository.IntegranteRepository integranteRepository;
    @Mock private com.estratego.domain.repository.DecisionCasoRepository decisionCasoRepository;

    @InjectMocks
    private SimulacionService simulacionService;

    private Usuario coordinador;

    @BeforeEach
    void setUp() {
        coordinador = new Usuario(COORDINADOR_ID, "Coordinador", CORREO_COORDINADOR,
                "DOC", "coordinador", "hash", Rol.DOCENTE);
        when(usuarioRepository.findByCorreo(CORREO_COORDINADOR)).thenReturn(Optional.of(coordinador));
    }

    @Test
    void crearSimulacionExitoso() {
        CrearSimulacionRequest request = new CrearSimulacionRequest(
                "Curso 2026-1",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30)
        );

        when(simulacionRepository.save(any(Simulacion.class))).thenAnswer(inv -> {
            Simulacion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        var response = simulacionService.crear(request, CORREO_COORDINADOR);

        assertEquals("Curso 2026-1", response.getNombre());
        assertEquals(EstadoSimulacion.BORRADOR, response.getEstado());
        assertEquals(COORDINADOR_ID, response.getIdUsuarioCoordinador());
    }

    @Test
    void crearSimulacionRechazaFechaFinAnterior() {
        CrearSimulacionRequest request = new CrearSimulacionRequest(
                "Curso 2026-1",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(1)
        );

        assertThrows(IllegalArgumentException.class,
                () -> simulacionService.crear(request, CORREO_COORDINADOR));
    }

    @Test
    void programarCambiaEstado() {
        Simulacion simulacion = new Simulacion(1L, COORDINADOR_ID, "Curso",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                EstadoSimulacion.BORRADOR);

        simulacionLista(1L, true);
        when(simulacionRepository.findById(1L)).thenReturn(Optional.of(simulacion));
        when(simulacionRepository.save(any(Simulacion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = simulacionService.programar(1L, CORREO_COORDINADOR);

        assertEquals(EstadoSimulacion.PROGRAMADA, response.getEstado());
    }

    @Test
    void programarRechazaSiNoEsBorrador() {
        Simulacion simulacion = new Simulacion(1L, COORDINADOR_ID, "Curso",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                EstadoSimulacion.PROGRAMADA);

        when(simulacionRepository.findById(1L)).thenReturn(Optional.of(simulacion));

        assertThrows(IllegalArgumentException.class,
                () -> simulacionService.programar(1L, CORREO_COORDINADOR));
    }

    @Test
    void iniciarCambiaEstado() {
        Simulacion simulacion = new Simulacion(1L, COORDINADOR_ID, "Curso",
                LocalDate.now(), LocalDate.now().plusDays(30),
                EstadoSimulacion.PROGRAMADA);

        when(simulacionRepository.findById(1L)).thenReturn(Optional.of(simulacion));
        when(simulacionRepository.save(any(Simulacion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = simulacionService.iniciar(1L, CORREO_COORDINADOR);

        assertEquals(EstadoSimulacion.EN_CURSO, response.getEstado());
    }

    @Test
    void finalizarCambiaEstado() {
        Simulacion simulacion = new Simulacion(1L, COORDINADOR_ID, "Curso",
                LocalDate.now().minusDays(1), LocalDate.now(),
                EstadoSimulacion.EN_CURSO);

        when(simulacionRepository.findById(1L)).thenReturn(Optional.of(simulacion));
        when(simulacionRepository.save(any(Simulacion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = simulacionService.finalizar(1L, CORREO_COORDINADOR);

        assertEquals(EstadoSimulacion.FINALIZADA, response.getEstado());
    }

    @Test
    void obtenerRechazaSimulacionDeOtroCoordinador() {
        Usuario otroCoordinador = new Usuario(888L, "Otro", "otro@test.com",
                "DOC999", "otro", "hash", Rol.DOCENTE);
        Simulacion simulacion = new Simulacion(1L, 888L, "Curso",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                EstadoSimulacion.BORRADOR);

        when(simulacionRepository.findById(1L)).thenReturn(Optional.of(simulacion));

        assertThrows(IllegalArgumentException.class,
                () -> simulacionService.obtener(1L, CORREO_COORDINADOR));
    }

    // ---- Simulación lista para jugarse: 1 caso con balance que cuadra y 1 empresa con líder ----

    private static com.estratego.domain.model.caso.Caso casoListo(Long idSim) {
        com.estratego.domain.model.caso.Caso c = new com.estratego.domain.model.caso.Caso();
        c.setId(idSim * 100);
        c.setIdSimulacion(idSim);
        c.setNombreEmpresa("Caso de " + idSim);
        c.setFechaInicio(java.time.LocalDateTime.of(2026, 10, 20, 8, 0));
        c.setFinanciero(new com.estratego.domain.model.financiero.EstadoFinanciero(
                new java.math.BigDecimal("200000"), new java.math.BigDecimal("150000"), new java.math.BigDecimal("250000"),
                new java.math.BigDecimal("350000"), new java.math.BigDecimal("50000"),
                new java.math.BigDecimal("150000"), new java.math.BigDecimal("100000"), new java.math.BigDecimal("150000"),
                new java.math.BigDecimal("400000"), new java.math.BigDecimal("100000"),
                new java.math.BigDecimal("800000"), new java.math.BigDecimal("500000"), new java.math.BigDecimal("80000"),
                new java.math.BigDecimal("60000"), new java.math.BigDecimal("20000"), new java.math.BigDecimal("40000"),
                new java.math.BigDecimal("120000"), new java.math.BigDecimal("-80000"), new java.math.BigDecimal("-20000")));
        return c;
    }

    private void simulacionLista(Long idSim, boolean conLider) {
        org.mockito.Mockito.lenient().when(casoRepository.findByIdSimulacion(idSim)).thenReturn(List.of(casoListo(idSim)));
        org.mockito.Mockito.lenient().when(empresaRepository.findByIdSimulacion(idSim)).thenReturn(List.of(
                new com.estratego.domain.model.empresa.Empresa(idSim * 10, idSim, "EMP-001", "Alfa", null,
                        com.estratego.domain.model.empresa.TipoJugador.MULTIUSUARIO,
                        com.estratego.domain.model.empresa.EstadoEmpresa.ACTIVA)));
        org.mockito.Mockito.lenient().when(integranteRepository.findByIdEmpresa(idSim * 10)).thenReturn(List.of(
                new com.estratego.domain.model.integrante.Integrante(1L, idSim * 10, 20L,
                        com.estratego.domain.model.integrante.Departamento.COMERCIAL, conLider)));
    }

    @Test
    void programarRechazaUnaSimulacionIncompletaConTodosLosProblemas() {
        Simulacion simulacion = new Simulacion(1L, COORDINADOR_ID, "Curso",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), EstadoSimulacion.BORRADOR);
        when(simulacionRepository.findById(1L)).thenReturn(Optional.of(simulacion));
        when(casoRepository.findByIdSimulacion(1L)).thenReturn(List.of());
        when(empresaRepository.findByIdSimulacion(1L)).thenReturn(List.of());

        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> simulacionService.programar(1L, CORREO_COORDINADOR));

        assertEquals("La simulación no está lista para programarse", ex.getMessage());
        assertEquals(List.of(
                new ValidacionException.Problema("casos", "La simulación no tiene casos"),
                new ValidacionException.Problema("empresas", "Ninguna empresa tiene integrantes")), ex.getProblemas());
        org.mockito.Mockito.verify(simulacionRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void programarRechazaEmpresasSinLiderYPrimerCasoQueNoCuadra() {
        Simulacion simulacion = new Simulacion(1L, COORDINADOR_ID, "Curso",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), EstadoSimulacion.BORRADOR);
        when(simulacionRepository.findById(1L)).thenReturn(Optional.of(simulacion));
        simulacionLista(1L, false);
        com.estratego.domain.model.caso.Caso noCuadra = casoListo(1L);
        noCuadra.getFinanciero().setEfectivo(new java.math.BigDecimal("999999"));
        when(casoRepository.findByIdSimulacion(1L)).thenReturn(List.of(noCuadra));

        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> simulacionService.programar(1L, CORREO_COORDINADOR));

        assertEquals(2, ex.getProblemas().size());
        assertEquals("El primer caso («Caso de 1») no tiene un balance inicial completo que cuadre",
                ex.getProblemas().get(0).mensaje());
        assertEquals("Estas empresas no tienen líder: EMP-001 (Alfa)", ex.getProblemas().get(1).mensaje());
    }

    @Test
    void desprogramarVuelveABorrador() {
        Simulacion simulacion = new Simulacion(1L, COORDINADOR_ID, "Curso",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), EstadoSimulacion.PROGRAMADA);
        when(simulacionRepository.findById(1L)).thenReturn(Optional.of(simulacion));
        when(casoRepository.findByIdSimulacion(1L)).thenReturn(List.of(casoListo(1L)));
        when(simulacionRepository.save(any(Simulacion.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(EstadoSimulacion.BORRADOR, simulacionService.desprogramar(1L, CORREO_COORDINADOR).getEstado());
    }

    @Test
    void desprogramarRechazaSiYaHayDecisiones() {
        Simulacion simulacion = new Simulacion(1L, COORDINADOR_ID, "Curso",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), EstadoSimulacion.PROGRAMADA);
        when(simulacionRepository.findById(1L)).thenReturn(Optional.of(simulacion));
        when(casoRepository.findByIdSimulacion(1L)).thenReturn(List.of(casoListo(1L)));
        when(decisionCasoRepository.existsByIdCaso(100L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> simulacionService.desprogramar(1L, CORREO_COORDINADOR));
        assertEquals("La simulación ya tiene decisiones de empresas; no se puede desprogramar", ex.getMessage());
    }

    @Test
    void desprogramarSoloDesdeProgramada() {
        Simulacion simulacion = new Simulacion(1L, COORDINADOR_ID, "Curso",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), EstadoSimulacion.BORRADOR);
        when(simulacionRepository.findById(1L)).thenReturn(Optional.of(simulacion));

        assertThrows(IllegalArgumentException.class, () -> simulacionService.desprogramar(1L, CORREO_COORDINADOR));
    }

    @Test
    void actualizarRechazaFechaInicioPasada() {
        Simulacion sim = new Simulacion(5L, COORDINADOR_ID, "Curso", LocalDate.now().plusDays(3), null,
                EstadoSimulacion.PROGRAMADA);
        when(simulacionRepository.findById(5L)).thenReturn(Optional.of(sim));

        var request = new ActualizarSimulacionRequest("Curso", LocalDate.now().minusDays(1), null);

        CampoInvalidoException ex = assertThrows(CampoInvalidoException.class,
                () -> simulacionService.actualizar(5L, request, CORREO_COORDINADOR));
        assertEquals("fechaInicio", ex.getCampo());
        assertEquals(LocalDate.now().plusDays(3), sim.getFechaInicio());
    }

    @Test
    void actualizarPermiteConservarUnaFechaInicioQueYaPaso() {
        LocalDate pasada = LocalDate.now().minusDays(2);
        Simulacion sim = new Simulacion(6L, COORDINADOR_ID, "Curso", pasada, null, EstadoSimulacion.BORRADOR);
        when(simulacionRepository.findById(6L)).thenReturn(Optional.of(sim));
        when(simulacionRepository.save(any(Simulacion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = simulacionService.actualizar(6L,
                new ActualizarSimulacionRequest("Curso renombrado", pasada, null), CORREO_COORDINADOR);

        assertEquals("Curso renombrado", response.getNombre());
    }
}

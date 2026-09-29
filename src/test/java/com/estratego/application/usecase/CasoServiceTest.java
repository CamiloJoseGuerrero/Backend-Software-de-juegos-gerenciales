package com.estratego.application.usecase;

import com.estratego.application.dto.docente.CasoRequest;
import com.estratego.application.dto.docente.FinancieroCaso;
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

    private CasoRequest requestValido() {
        LocalDateTime inicio = INICIO_SIM.atTime(8, 0);
        return new CasoRequest(
                null, "TextilAndes S.A.", "Manufactura", "Vestir a Colombia", "Líder regional en 2030",
                new FinancieroCaso(new BigDecimal("1000000"), new BigDecimal("400000"),
                        new BigDecimal("600000"), new BigDecimal("50000")),
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
        assertEquals(new BigDecimal("600000"), r.getFinanciero().getPatrimonio());
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
}

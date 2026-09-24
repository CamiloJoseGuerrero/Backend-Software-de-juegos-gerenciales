package com.estratego.application.usecase;

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
}
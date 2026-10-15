package com.estratego.application.usecase;

import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Ciclo de vida automático de la simulación (lo dispara el cierre automático cada minuto). */
@ExtendWith(MockitoExtension.class)
class SimulacionAutomaticaTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 20);

    @Mock private SimulacionRepository simulacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SimulacionService simulacionService;

    private static Simulacion sim(Long id, LocalDate inicio, LocalDate fin, EstadoSimulacion estado) {
        return new Simulacion(id, 99L, "Sim " + id, inicio, fin, estado);
    }

    @Test
    void iniciaLasProgramadasCuyaFechaDeInicioYaLlego() {
        Simulacion hoy = sim(1L, HOY, HOY.plusDays(10), EstadoSimulacion.PROGRAMADA);
        Simulacion ayer = sim(2L, HOY.minusDays(1), HOY.plusDays(10), EstadoSimulacion.PROGRAMADA);
        Simulacion manana = sim(3L, HOY.plusDays(1), HOY.plusDays(10), EstadoSimulacion.PROGRAMADA);
        when(simulacionRepository.findByEstado(EstadoSimulacion.PROGRAMADA)).thenReturn(List.of(hoy, ayer, manana));
        when(simulacionRepository.findByEstado(EstadoSimulacion.EN_CURSO)).thenReturn(List.of());

        List<Long> cambiadas = simulacionService.avanzarAutomaticamente(HOY);

        assertEquals(List.of(1L, 2L), cambiadas);
        assertEquals(EstadoSimulacion.EN_CURSO, hoy.getEstado());
        assertEquals(EstadoSimulacion.EN_CURSO, ayer.getEstado());
        assertEquals(EstadoSimulacion.PROGRAMADA, manana.getEstado());
        verify(simulacionRepository, times(2)).save(any(Simulacion.class));
    }

    @Test
    void finalizaLasEnCursoCuandoTerminaElDiaDeSuFechaDeFin() {
        Simulacion termino = sim(4L, HOY.minusDays(10), HOY.minusDays(1), EstadoSimulacion.EN_CURSO);
        Simulacion terminaHoy = sim(5L, HOY.minusDays(10), HOY, EstadoSimulacion.EN_CURSO);
        Simulacion sinFin = sim(6L, HOY.minusDays(10), null, EstadoSimulacion.EN_CURSO);
        when(simulacionRepository.findByEstado(EstadoSimulacion.PROGRAMADA)).thenReturn(List.of());
        when(simulacionRepository.findByEstado(EstadoSimulacion.EN_CURSO)).thenReturn(List.of(termino, terminaHoy, sinFin));

        List<Long> cambiadas = simulacionService.avanzarAutomaticamente(HOY);

        assertEquals(List.of(4L), cambiadas);
        assertEquals(EstadoSimulacion.FINALIZADA, termino.getEstado());
        assertEquals(EstadoSimulacion.EN_CURSO, terminaHoy.getEstado());
        assertEquals(EstadoSimulacion.EN_CURSO, sinFin.getEstado());
    }

    @Test
    void sinNadaQueCambiarNoGuardaNada() {
        when(simulacionRepository.findByEstado(any())).thenReturn(List.of());

        assertTrue(simulacionService.avanzarAutomaticamente(HOY).isEmpty());
        verify(simulacionRepository, never()).save(any());
    }

    @Test
    void iniciarAManoUnaQueYaSeInicioSolaNoFalla() {
        when(usuarioRepository.findByCorreo("docente@test.com")).thenReturn(java.util.Optional.of(
                new com.estratego.domain.model.usuario.Usuario(99L, "Docente", "docente@test.com", "D", "docente", "h",
                        com.estratego.domain.model.usuario.Rol.DOCENTE)));
        Simulacion enCurso = sim(7L, HOY.minusDays(1), HOY.plusDays(10), EstadoSimulacion.EN_CURSO);
        when(simulacionRepository.findById(7L)).thenReturn(java.util.Optional.of(enCurso));

        var r = simulacionService.iniciar(7L, "docente@test.com");

        assertEquals(EstadoSimulacion.EN_CURSO, r.getEstado());
        verify(simulacionRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}

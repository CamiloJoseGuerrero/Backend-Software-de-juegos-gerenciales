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
    @Mock private com.estratego.domain.repository.CasoRepository casoRepository;
    @Mock private com.estratego.domain.repository.EmpresaRepository empresaRepository;
    @Mock private com.estratego.domain.repository.IntegranteRepository integranteRepository;
    @Mock private com.estratego.domain.repository.DecisionCasoRepository decisionCasoRepository;

    @InjectMocks
    private SimulacionService simulacionService;

    private static Simulacion sim(Long id, LocalDate inicio, LocalDate fin, EstadoSimulacion estado) {
        return new Simulacion(id, 99L, "Sim " + id, inicio, fin, estado);
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
    void iniciaLasProgramadasCuyaFechaDeInicioYaLlego() {
        simulacionLista(1L, true);
        simulacionLista(2L, true);
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

    @Test
    void unaProgramadaQueYaNoEstaListaNoArranca() {
        Simulacion sinLider = sim(8L, HOY, HOY.plusDays(10), EstadoSimulacion.PROGRAMADA);
        Simulacion sinCasos = sim(9L, HOY, HOY.plusDays(10), EstadoSimulacion.PROGRAMADA);
        simulacionLista(8L, false);
        when(simulacionRepository.findByEstado(EstadoSimulacion.PROGRAMADA)).thenReturn(List.of(sinLider, sinCasos));
        when(simulacionRepository.findByEstado(EstadoSimulacion.EN_CURSO)).thenReturn(List.of());

        assertTrue(simulacionService.avanzarAutomaticamente(HOY).isEmpty());
        assertEquals(EstadoSimulacion.PROGRAMADA, sinLider.getEstado());
        assertEquals(EstadoSimulacion.PROGRAMADA, sinCasos.getEstado());
        verify(simulacionRepository, never()).save(any());
    }
}

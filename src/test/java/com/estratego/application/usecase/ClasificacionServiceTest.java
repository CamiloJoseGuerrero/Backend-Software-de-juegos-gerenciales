package com.estratego.application.usecase;

import com.estratego.application.dto.clasificacion.ClasificacionResponse;
import com.estratego.domain.model.caso.*;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.financiero.EstadoFinanciero;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClasificacionServiceTest {

    private static final String DOCENTE = "docente@test.com";
    private static final String ESTUDIANTE = "ana@test.com";
    private static final Long DOCENTE_ID = 99L;
    private static final Long ESTUDIANTE_ID = 20L;
    private static final Long SIM_ID = 10L;

    @Mock private SimulacionRepository simulacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private IntegranteRepository integranteRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private CasoRepository casoRepository;
    @Mock private DecisionCasoRepository decisionCasoRepository;

    @InjectMocks
    private ClasificacionService service;

    private Simulacion simulacion;
    private final LocalDateTime ahora = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        simulacion = new Simulacion(SIM_ID, DOCENTE_ID, "Gerencia 2026-2", LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(30), EstadoSimulacion.EN_CURSO);
        lenient().when(simulacionRepository.findById(SIM_ID)).thenReturn(Optional.of(simulacion));
        lenient().when(usuarioRepository.findByCorreo(DOCENTE)).thenReturn(Optional.of(
                new Usuario(DOCENTE_ID, "Docente", DOCENTE, "D", "docente", "h", Rol.DOCENTE)));
        lenient().when(usuarioRepository.findByCorreo(ESTUDIANTE)).thenReturn(Optional.of(
                new Usuario(ESTUDIANTE_ID, "Ana", ESTUDIANTE, "1", "ana", "h", Rol.ESTUDIANTE)));
        lenient().when(empresaRepository.findByIdSimulacion(SIM_ID)).thenReturn(List.of(
                empresa(1L, "EMP-001"), empresa(2L, "EMP-002"), empresa(3L, "EMP-003")));
    }

    private static Empresa empresa(Long id, String codigo) {
        return new Empresa(id, SIM_ID, codigo, "Empresa " + codigo, null, TipoJugador.MULTIUSUARIO, EstadoEmpresa.ACTIVA);
    }

    /** Utilidad base 100.000, penalización máx. 8 %; opción 1 = ventas +15 %, opción 2 sin impacto. */
    private Caso caso(Long id, LocalDateTime fin, LocalDateTime activadoEn) {
        EstadoFinanciero ef = new EstadoFinanciero();
        ef.setVentasNetas(new BigDecimal("800000"));
        ef.setCostoVentas(new BigDecimal("500000"));
        ef.setGastosAdministracion(new BigDecimal("80000"));
        ef.setGastosVentas(new BigDecimal("60000"));
        ef.setGastosFinancieros(new BigDecimal("20000"));
        ef.setImpuestoRenta(new BigDecimal("40000"));
        Caso c = new Caso();
        c.setId(id);
        c.setIdSimulacion(SIM_ID);
        c.setNombreEmpresa("TextilAndes " + id);
        c.setFinanciero(ef);
        c.setUtilidadNeta(ef.utilidadNeta());
        c.setPenalizacionMin(new BigDecimal("2"));
        c.setPenalizacionMax(new BigDecimal("8"));
        c.setFechaInicio(fin.minusHours(2));
        c.setFechaFin(fin);
        c.setActivadoEn(activadoEn);
        c.setOpciones(new ArrayList<>(List.of(
                new OpcionCaso(id * 10 + 1, 1, "Ampliar", "r1",
                        new ImpactoOpcion(new ImpactoDriver(TipoImpacto.PORCENTAJE, new BigDecimal("15")),
                                null, null, null, null, null)),
                new OpcionCaso(id * 10 + 2, 2, "Esperar", "r2"))));
        return c;
    }

    private static DecisionCaso decision(Long idCaso, Long idEmpresa, Long idOpcion) {
        return new DecisionCaso(null, idCaso, idEmpresa, idOpcion, 20L, LocalDateTime.now());
    }

    @Test
    void sinCasosTerminadosDevuelveListaVacia() {
        Caso enCurso = caso(7L, ahora.plusHours(1), ahora.minusHours(1));
        when(casoRepository.findByIdSimulacion(SIM_ID)).thenReturn(List.of(enCurso));

        ClasificacionResponse r = service.paraDocente(SIM_ID, DOCENTE);

        assertEquals(0, r.getCasosConsiderados());
        assertTrue(r.getClasificacion().isEmpty());
        assertFalse(r.isDefinitiva());
    }

    @Test
    void sumaPorEmpresaAplicaImpactoYPenalizaAQuienNoDecidio() {
        Caso c1 = caso(7L, ahora.minusDays(2), ahora.minusDays(3));
        Caso c2 = caso(8L, ahora.minusDays(1), ahora.minusDays(2));
        Caso nuncaActivado = caso(9L, ahora.minusHours(5), null);
        when(casoRepository.findByIdSimulacion(SIM_ID)).thenReturn(List.of(c1, c2, nuncaActivado));
        // Caso 7: la empresa 1 elige la opción con +15 % ventas (800.000 × 1,15 = 920.000 → utilidad 220.000), la 2 la opción sin impacto
        when(decisionCasoRepository.findByIdCaso(7L)).thenReturn(List.of(decision(7L, 1L, 71L), decision(7L, 2L, 72L)));
        // Caso 8: la 1 vuelve a elegir la opción con impacto; la 2 no decide; la 3 nunca decide
        when(decisionCasoRepository.findByIdCaso(8L)).thenReturn(List.of(decision(8L, 1L, 81L)));

        ClasificacionResponse r = service.paraDocente(SIM_ID, DOCENTE);

        assertEquals(2, r.getCasosConsiderados());
        var f = r.getClasificacion();
        assertEquals(1L, f.get(0).getIdEmpresa());
        assertEquals(new BigDecimal("440000.00"), f.get(0).getUtilidadAcumulada()); // 220.000 × 2
        assertEquals(1, f.get(0).getPosicion());
        assertEquals(2L, f.get(1).getIdEmpresa());
        assertEquals(new BigDecimal("192000.00"), f.get(1).getUtilidadAcumulada()); // 100.000 + 92.000
        assertEquals(1, f.get(1).getCasosSinDecision());
        assertEquals(3L, f.get(2).getIdEmpresa());
        assertEquals(new BigDecimal("184000.00"), f.get(2).getUtilidadAcumulada()); // 92.000 × 2
        assertEquals(3, f.get(2).getPosicion());

        var desglose = f.get(1).getDesglose();
        assertEquals(2, desglose.size());
        assertTrue(desglose.get(0).isDecidio());
        assertEquals("Esperar", desglose.get(0).getOpcionElegida());
        assertFalse(desglose.get(1).isDecidio());
        assertEquals(new BigDecimal("8"), desglose.get(1).getPenalizacionPorcentaje());
        assertEquals("TextilAndes 8", desglose.get(1).getNombreCaso());
    }

    @Test
    void empatesComparteLaPosicion() {
        Caso c1 = caso(7L, ahora.minusDays(1), ahora.minusDays(2));
        when(casoRepository.findByIdSimulacion(SIM_ID)).thenReturn(List.of(c1));
        when(decisionCasoRepository.findByIdCaso(7L)).thenReturn(List.of(
                decision(7L, 1L, 72L), decision(7L, 2L, 72L)));

        var f = service.paraDocente(SIM_ID, DOCENTE).getClasificacion();

        assertEquals(1, f.get(0).getPosicion());
        assertEquals(1, f.get(1).getPosicion());
        assertEquals(3, f.get(2).getPosicion());
    }

    @Test
    void alFinalizarCuentanTodosLosCasosActivados() {
        simulacion.setEstado(EstadoSimulacion.FINALIZADA);
        Caso todaviaEnPlazo = caso(7L, ahora.plusHours(3), ahora.minusHours(1));
        when(casoRepository.findByIdSimulacion(SIM_ID)).thenReturn(List.of(todaviaEnPlazo));
        when(decisionCasoRepository.findByIdCaso(7L)).thenReturn(List.of());

        ClasificacionResponse r = service.paraDocente(SIM_ID, DOCENTE);

        assertTrue(r.isDefinitiva());
        assertEquals(1, r.getCasosConsiderados());
    }

    @Test
    void estudianteNoLaVeAntesDeFinalizar() {
        when(integranteRepository.existsEnSimulacion(SIM_ID, ESTUDIANTE_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.paraEstudiante(SIM_ID, ESTUDIANTE));
        assertEquals("La clasificación estará disponible cuando la simulación finalice", ex.getMessage());
    }

    @Test
    void estudianteQueNoParticipaNoLaVe() {
        simulacion.setEstado(EstadoSimulacion.FINALIZADA);
        when(integranteRepository.existsEnSimulacion(SIM_ID, ESTUDIANTE_ID)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.paraEstudiante(SIM_ID, ESTUDIANTE));
        verify(casoRepository, never()).findByIdSimulacion(any());
    }

    @Test
    void docenteDeOtraSimulacionNoLaVe() {
        simulacion.setIdUsuarioCoordinador(1234L);

        assertThrows(IllegalArgumentException.class, () -> service.paraDocente(SIM_ID, DOCENTE));
    }
}

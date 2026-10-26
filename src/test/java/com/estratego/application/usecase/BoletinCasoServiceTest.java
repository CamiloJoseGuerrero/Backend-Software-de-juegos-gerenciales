package com.estratego.application.usecase;

import com.estratego.application.dto.docente.BoletinCasoRequest;
import com.estratego.application.dto.docente.BoletinCasoResponse;
import com.estratego.domain.model.boletin.BoletinCaso;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.BoletinCasoRepository;
import com.estratego.domain.repository.CasoRepository;
import com.estratego.domain.repository.IntegranteRepository;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoletinCasoServiceTest {

    private static final String CORREO_DOCENTE = "docente@test.com";
    private static final String CORREO_ESTUDIANTE = "estudiante@test.com";
    private static final Long DOCENTE_ID = 1L;
    private static final Long OTRO_DOCENTE_ID = 2L;
    private static final Long ESTUDIANTE_ID = 10L;
    private static final Long SIMULACION_ID = 100L;
    private static final Long CASO_ID = 200L;

    @Mock
    private BoletinCasoRepository boletinCasoRepository;

    @Mock
    private CasoRepository casoRepository;

    @Mock
    private SimulacionRepository simulacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private IntegranteRepository integranteRepository;

    @InjectMocks
    private BoletinCasoService boletinCasoService;

    private Usuario docente;
    private Usuario estudiante;
    private Simulacion simulacion;
    private Caso caso;

    @BeforeEach
    void setUp() {
        docente = new Usuario(DOCENTE_ID, "Docente Prueba", CORREO_DOCENTE, "12345", "docente", "hash", Rol.DOCENTE);
        estudiante = new Usuario(ESTUDIANTE_ID, "Estudiante Prueba", CORREO_ESTUDIANTE, "67890", "estudiante", "hash", Rol.ESTUDIANTE);

        simulacion = new Simulacion();
        simulacion.setId(SIMULACION_ID);
        simulacion.setIdUsuarioCoordinador(DOCENTE_ID);
        simulacion.setEstado(EstadoSimulacion.EN_CURSO);

        caso = new Caso();
        caso.setId(CASO_ID);
        caso.setIdSimulacion(SIMULACION_ID);
        caso.setFechaVisualizacion(LocalDateTime.now().minusHours(1));
    }

    @Test
    void guardarOReemplazarCreaNuevoBoletin() {
        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
        when(boletinCasoRepository.findByIdCaso(CASO_ID)).thenReturn(Optional.empty());
        when(boletinCasoRepository.save(any(BoletinCaso.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletinCasoRequest req = new BoletinCasoRequest(
                new BigDecimal("5.2"),
                new BigDecimal("11.5"),
                new BigDecimal("4120.50"),
                "  Entorno volátil  "
        );

        BoletinCasoResponse res = boletinCasoService.guardarOReemplazar(CASO_ID, req, CORREO_DOCENTE);

        assertEquals(CASO_ID, res.getIdCaso());
        assertEquals(new BigDecimal("5.2"), res.getInflacion());
        assertEquals(new BigDecimal("11.5"), res.getTasaInteres());
        assertEquals(new BigDecimal("4120.50"), res.getTasaCambio());
        assertEquals("Entorno volátil", res.getNota());
        assertNotNull(res.getPublicadoEn());
        assertNotNull(res.getActualizadoEn());

        ArgumentCaptor<BoletinCaso> captor = ArgumentCaptor.forClass(BoletinCaso.class);
        verify(boletinCasoRepository).save(captor.capture());
        BoletinCaso guardado = captor.getValue();
        assertEquals(CASO_ID, guardado.getIdCaso());
        assertEquals("Entorno volátil", guardado.getNota());
    }

    @Test
    void guardarOReemplazarActualizaBoletinExistenteYPreservaPublicadoEn() {
        LocalDateTime publicadoEnOriginal = LocalDateTime.now().minusDays(2);
        BoletinCaso existente = new BoletinCaso(
                50L, CASO_ID,
                new BigDecimal("4.0"), new BigDecimal("10.0"), new BigDecimal("4000.00"),
                "Nota vieja", publicadoEnOriginal, publicadoEnOriginal
        );

        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
        when(boletinCasoRepository.findByIdCaso(CASO_ID)).thenReturn(Optional.of(existente));
        when(boletinCasoRepository.save(any(BoletinCaso.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletinCasoRequest req = new BoletinCasoRequest(
                new BigDecimal("6.0"),
                new BigDecimal("12.0"),
                new BigDecimal("4200.00"),
                "Nota nueva"
        );

        BoletinCasoResponse res = boletinCasoService.guardarOReemplazar(CASO_ID, req, CORREO_DOCENTE);

        assertEquals(new BigDecimal("6.0"), res.getInflacion());
        assertEquals(new BigDecimal("12.0"), res.getTasaInteres());
        assertEquals(new BigDecimal("4200.00"), res.getTasaCambio());
        assertEquals("Nota nueva", res.getNota());
        assertEquals(publicadoEnOriginal, res.getPublicadoEn());
        assertTrue(res.getActualizadoEn().isAfter(publicadoEnOriginal));
    }

    @Test
    void guardarOReemplazarNotaVaciaGuardaNull() {
        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
        when(boletinCasoRepository.findByIdCaso(CASO_ID)).thenReturn(Optional.empty());
        when(boletinCasoRepository.save(any(BoletinCaso.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletinCasoRequest req = new BoletinCasoRequest(
                new BigDecimal("3.0"),
                new BigDecimal("8.0"),
                new BigDecimal("3900.00"),
                "    "
        );

        BoletinCasoResponse res = boletinCasoService.guardarOReemplazar(CASO_ID, req, CORREO_DOCENTE);
        assertNull(res.getNota());
    }

    @Test
    void guardarOReemplazarSimulacionFinalizadaFalla() {
        simulacion.setEstado(EstadoSimulacion.FINALIZADA);

        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));

        BoletinCasoRequest req = new BoletinCasoRequest(
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("4000"), null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> boletinCasoService.guardarOReemplazar(CASO_ID, req, CORREO_DOCENTE));
        assertTrue(ex.getMessage().contains("finalizada"));
    }

    @Test
    void guardarOReemplazarSimulacionDeOtroDocenteFalla() {
        simulacion.setIdUsuarioCoordinador(OTRO_DOCENTE_ID);

        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));

        BoletinCasoRequest req = new BoletinCasoRequest(
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("4000"), null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> boletinCasoService.guardarOReemplazar(CASO_ID, req, CORREO_DOCENTE));
        assertTrue(ex.getMessage().contains("no pertenece a una simulación de este docente"));
    }

    @Test
    void guardarOReemplazarValidacionesNumericas() {
        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));

        // Inflación menor a -100
        BoletinCasoRequest r1 = new BoletinCasoRequest(new BigDecimal("-100.01"), BigDecimal.ZERO, BigDecimal.TEN, null);
        assertThrows(CampoInvalidoException.class, () -> boletinCasoService.guardarOReemplazar(CASO_ID, r1, CORREO_DOCENTE));

        // Inflación mayor a 1000
        BoletinCasoRequest r2 = new BoletinCasoRequest(new BigDecimal("1000.01"), BigDecimal.ZERO, BigDecimal.TEN, null);
        assertThrows(CampoInvalidoException.class, () -> boletinCasoService.guardarOReemplazar(CASO_ID, r2, CORREO_DOCENTE));

        // Tasa de interés menor a 0
        BoletinCasoRequest r3 = new BoletinCasoRequest(BigDecimal.ZERO, new BigDecimal("-0.01"), BigDecimal.TEN, null);
        assertThrows(CampoInvalidoException.class, () -> boletinCasoService.guardarOReemplazar(CASO_ID, r3, CORREO_DOCENTE));

        // Tasa de interés mayor a 1000
        BoletinCasoRequest r4 = new BoletinCasoRequest(BigDecimal.ZERO, new BigDecimal("1000.01"), BigDecimal.TEN, null);
        assertThrows(CampoInvalidoException.class, () -> boletinCasoService.guardarOReemplazar(CASO_ID, r4, CORREO_DOCENTE));

        // Tasa de cambio igual a 0
        BoletinCasoRequest r5 = new BoletinCasoRequest(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null);
        assertThrows(CampoInvalidoException.class, () -> boletinCasoService.guardarOReemplazar(CASO_ID, r5, CORREO_DOCENTE));

        // Tasa de cambio negativa
        BoletinCasoRequest r6 = new BoletinCasoRequest(BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("-5"), null);
        assertThrows(CampoInvalidoException.class, () -> boletinCasoService.guardarOReemplazar(CASO_ID, r6, CORREO_DOCENTE));

        // Nota mayor a 2000 caracteres
        String notaLarga = "a".repeat(2001);
        BoletinCasoRequest r7 = new BoletinCasoRequest(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, notaLarga);
        assertThrows(CampoInvalidoException.class, () -> boletinCasoService.guardarOReemplazar(CASO_ID, r7, CORREO_DOCENTE));
    }

    @Test
    void obtenerParaDocenteExistente() {
        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));

        BoletinCaso b = new BoletinCaso(1L, CASO_ID, BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("4000"), null, LocalDateTime.now(), LocalDateTime.now());
        when(boletinCasoRepository.findByIdCaso(CASO_ID)).thenReturn(Optional.of(b));

        Optional<BoletinCasoResponse> res = boletinCasoService.obtenerParaDocente(CASO_ID, CORREO_DOCENTE);
        assertTrue(res.isPresent());
        assertEquals(CASO_ID, res.get().getIdCaso());
    }

    @Test
    void obtenerParaDocenteInexistenteRetornaVacio() {
        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
        when(boletinCasoRepository.findByIdCaso(CASO_ID)).thenReturn(Optional.empty());

        Optional<BoletinCasoResponse> res = boletinCasoService.obtenerParaDocente(CASO_ID, CORREO_DOCENTE);
        assertTrue(res.isEmpty());
    }

    @Test
    void eliminarParaDocenteExitoso() {
        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));

        boletinCasoService.eliminarParaDocente(CASO_ID, CORREO_DOCENTE);
        verify(boletinCasoRepository).deleteByIdCaso(CASO_ID);
    }

    @Test
    void eliminarParaDocenteSimulacionFinalizadaFalla() {
        simulacion.setEstado(EstadoSimulacion.FINALIZADA);
        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(simulacionRepository.findById(SIMULACION_ID)).thenReturn(Optional.of(simulacion));
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));

        assertThrows(IllegalArgumentException.class,
                () -> boletinCasoService.eliminarParaDocente(CASO_ID, CORREO_DOCENTE));
        verify(boletinCasoRepository, never()).deleteByIdCaso(anyLong());
    }

    @Test
    void obtenerParaEstudianteVisibleYExistente() {
        caso.setFechaVisualizacion(LocalDateTime.now().minusHours(2));

        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo(CORREO_ESTUDIANTE)).thenReturn(Optional.of(estudiante));
        when(integranteRepository.existsEnSimulacion(SIMULACION_ID, ESTUDIANTE_ID)).thenReturn(true);

        BoletinCaso b = new BoletinCaso(1L, CASO_ID, BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("4000"), "Nota", LocalDateTime.now(), LocalDateTime.now());
        when(boletinCasoRepository.findByIdCaso(CASO_ID)).thenReturn(Optional.of(b));

        Optional<BoletinCasoResponse> res = boletinCasoService.obtenerParaEstudiante(CASO_ID, CORREO_ESTUDIANTE);
        assertTrue(res.isPresent());
        assertEquals("Nota", res.get().getNota());
    }

    @Test
    void obtenerParaEstudianteAntesDeFechaVisualizacionRetornaVacio() {
        caso.setFechaVisualizacion(LocalDateTime.now().plusHours(2));

        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo(CORREO_ESTUDIANTE)).thenReturn(Optional.of(estudiante));
        when(integranteRepository.existsEnSimulacion(SIMULACION_ID, ESTUDIANTE_ID)).thenReturn(true);

        Optional<BoletinCasoResponse> res = boletinCasoService.obtenerParaEstudiante(CASO_ID, CORREO_ESTUDIANTE);
        assertTrue(res.isEmpty());
        verify(boletinCasoRepository, never()).findByIdCaso(anyLong());
    }

    @Test
    void obtenerParaEstudianteNoParticipaEnSimulacionFalla() {
        when(casoRepository.findById(CASO_ID)).thenReturn(Optional.of(caso));
        when(usuarioRepository.findByCorreo(CORREO_ESTUDIANTE)).thenReturn(Optional.of(estudiante));
        when(integranteRepository.existsEnSimulacion(SIMULACION_ID, ESTUDIANTE_ID)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> boletinCasoService.obtenerParaEstudiante(CASO_ID, CORREO_ESTUDIANTE));
        assertTrue(ex.getMessage().contains("no pertenece a una simulación del estudiante"));
    }
}

package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarEquipoRequest;
import com.estratego.application.dto.docente.CrearEquipoRequest;
import com.estratego.domain.model.equipo.Equipo;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.DocenteEstudianteRepository;
import com.estratego.domain.repository.EquipoRepository;
import com.estratego.domain.repository.PartidaRepository;
import com.estratego.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    private static final String CORREO_DOCENTE = "docente@test.com";
    private static final Long DOCENTE_ID = 99L;

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private DocenteEstudianteRepository docenteEstudianteRepository;

    @InjectMocks
    private EquipoService equipoService;

    @BeforeEach
    void setUp() {
        Usuario docente = new Usuario(DOCENTE_ID, "Docente", CORREO_DOCENTE, "DOC", "docente", "hash", Rol.DOCENTE);
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
        // Por defecto, los estudiantes de prueba los cargó este docente
        lenient().when(docenteEstudianteRepository.existeVinculo(eq(DOCENTE_ID), anyLong())).thenReturn(true);
    }

    @Test
    void crearEquipoExitosoConNombreAutogenerado() {
        CrearEquipoRequest request = new CrearEquipoRequest(List.of(1L, 2L, 3L), 1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(estudiante(2L)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(estudiante(3L)));
        when(equipoRepository.findByDocenteId(DOCENTE_ID)).thenReturn(List.of());
        when(equipoRepository.countByDocenteId(DOCENTE_ID)).thenReturn(0L);
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(inv -> {
            Equipo e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        var response = equipoService.crear(request, CORREO_DOCENTE);

        assertEquals("Equipo 1", response.getNombre());
        assertEquals(1L, response.getLiderId());
        assertEquals(3, response.getEstudianteIds().size());
    }

    @Test
    void crearEquipoRechazaLiderQueNoEstaEnElEquipo() {
        CrearEquipoRequest request = new CrearEquipoRequest(List.of(1L, 2L), 7L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipoService.crear(request, CORREO_DOCENTE));
        assertEquals("El líder debe ser uno de los estudiantes del equipo", ex.getMessage());
        verify(equipoRepository, never()).save(any());
    }

    @Test
    void crearEquipoRechazaEstudianteDuplicado() {
        CrearEquipoRequest request = new CrearEquipoRequest(List.of(1L, 1L), 1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipoService.crear(request, CORREO_DOCENTE));
        assertEquals("No se pueden repetir estudiantes en el equipo", ex.getMessage());
    }

    @Test
    void crearEquipoRechazaUsuarioQueNoEsEstudiante() {
        CrearEquipoRequest request = new CrearEquipoRequest(List.of(1L, 5L), 1L);
        Usuario otroDocente = new Usuario(5L, "Otro", "otro@test.com", "D5", "otro", "hash", Rol.DOCENTE);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante(1L)));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(otroDocente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipoService.crear(request, CORREO_DOCENTE));
        assertEquals("El usuario 5 no es estudiante", ex.getMessage());
    }

    @Test
    void crearEquipoRechazaEstudianteDeOtroDocente() {
        CrearEquipoRequest request = new CrearEquipoRequest(List.of(1L, 2L), 1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(estudiante(2L)));
        when(docenteEstudianteRepository.existeVinculo(DOCENTE_ID, 2L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipoService.crear(request, CORREO_DOCENTE));

        assertEquals("El estudiante 2 no está en tu lista de estudiantes", ex.getMessage());
        verify(equipoRepository, never()).save(any());
    }

    @Test
    void crearEquipoRechazaEstudianteInexistente() {
        CrearEquipoRequest request = new CrearEquipoRequest(List.of(1L), 1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> equipoService.crear(request, CORREO_DOCENTE));
    }

    @Test
    void crearEquipoRechazaEstudianteYaAsignado() {
        CrearEquipoRequest request = new CrearEquipoRequest(List.of(1L), 1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante(1L)));
        when(equipoRepository.findByDocenteId(DOCENTE_ID))
                .thenReturn(List.of(new Equipo(1L, "Equipo 1", DOCENTE_ID, 1L, List.of(1L))));

        assertThrows(IllegalArgumentException.class,
                () -> equipoService.crear(request, CORREO_DOCENTE));
    }

    @Test
    void actualizarEquipoPermiteCambiarLider() {
        ActualizarEquipoRequest request = new ActualizarEquipoRequest(List.of(1L, 2L), 2L);
        Equipo existente = new Equipo(1L, "Equipo 1", DOCENTE_ID, 1L, List.of(1L, 2L));

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(estudiante(2L)));
        when(equipoRepository.findByDocenteId(DOCENTE_ID)).thenReturn(List.of(existente));
        when(equipoRepository.save(any(Equipo.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = equipoService.actualizar(1L, request, CORREO_DOCENTE);

        assertEquals(2L, response.getLiderId());
    }

    @Test
    void eliminarEquipoSinPartidas() {
        Equipo existente = new Equipo(1L, "Equipo 1", DOCENTE_ID, 1L, List.of(1L));
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(partidaRepository.existeEquipoEnAlgunaPartida(1L)).thenReturn(false);

        equipoService.eliminar(1L, CORREO_DOCENTE);

        verify(equipoRepository).deleteById(1L);
    }

    @Test
    void eliminarEquipoAsignadoAPartidaFalla() {
        Equipo existente = new Equipo(1L, "Equipo 1", DOCENTE_ID, 1L, List.of(1L));
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(partidaRepository.existeEquipoEnAlgunaPartida(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> equipoService.eliminar(1L, CORREO_DOCENTE));
        verify(equipoRepository, never()).deleteById(any());
    }

    @Test
    void eliminarEquipoDeOtroDocenteFalla() {
        Equipo ajeno = new Equipo(1L, "Equipo 1", 1234L, 1L, List.of(1L));
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(ajeno));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipoService.eliminar(1L, CORREO_DOCENTE));
        assertEquals("El equipo no pertenece a este docente", ex.getMessage());
        verify(equipoRepository, never()).deleteById(any());
    }

    @Test
    void eliminarEquipoInexistenteFalla() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> equipoService.eliminar(1L, CORREO_DOCENTE));
    }

    private Usuario estudiante(Long id) {
        return new Usuario(id, "Estudiante " + id, "est" + id + "@test.com",
                "E" + id, "est" + id, "hash", Rol.ESTUDIANTE);
    }
}

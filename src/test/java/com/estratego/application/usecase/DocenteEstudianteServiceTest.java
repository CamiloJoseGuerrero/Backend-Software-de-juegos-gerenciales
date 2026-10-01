package com.estratego.application.usecase;

import com.estratego.application.dto.docente.EstudianteResponse;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.DocenteEstudianteRepository;
import com.estratego.domain.repository.EstudianteRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocenteEstudianteServiceTest {

    private static final String CORREO_DOCENTE = "docente@test.com";
    private static final Long DOCENTE_ID = 99L;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EstudianteRepository estudianteRepository;
    @Mock private DocenteEstudianteRepository docenteEstudianteRepository;

    @InjectMocks
    private DocenteEstudianteService service;

    @BeforeEach
    void setUp() {
        Usuario docente = new Usuario(DOCENTE_ID, "Docente", CORREO_DOCENTE, "DOC", "docente", "hash", Rol.DOCENTE);
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
    }

    private Usuario estudiante(Long id) {
        return new Usuario(id, "Est " + id, "est" + id + "@test.com", "ID" + id, "est" + id, "hash", Rol.ESTUDIANTE);
    }

    @Test
    void listaSoloLosEstudiantesDelDocente() {
        when(docenteEstudianteRepository.findIdsEstudiantes(DOCENTE_ID)).thenReturn(List.of(1L, 2L));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(estudiante(2L)));

        List<EstudianteResponse> lista = service.listarEstudiantesDelDocente(CORREO_DOCENTE);

        assertEquals(List.of(1L, 2L), lista.stream().map(EstudianteResponse::getId).toList());
        verify(usuarioRepository, never()).findByRol(any());
    }

    @Test
    void obtenerEstudianteDeOtroDocenteResponde404Generico() {
        when(docenteEstudianteRepository.existeVinculo(DOCENTE_ID, 7L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.obtenerEstudiante(7L, CORREO_DOCENTE));

        assertEquals("Estudiante no encontrado", ex.getMessage());
        verify(usuarioRepository, never()).findById(7L);
    }

    @Test
    void obtenerEstudiantePropio() {
        when(docenteEstudianteRepository.existeVinculo(DOCENTE_ID, 1L)).thenReturn(true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante(1L)));

        EstudianteResponse r = service.obtenerEstudiante(1L, CORREO_DOCENTE);

        assertEquals("est1@test.com", r.getCorreo());
    }
}

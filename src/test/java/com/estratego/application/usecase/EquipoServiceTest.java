package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarEquipoRequest;
import com.estratego.application.dto.docente.CrearEquipoRequest;
import com.estratego.domain.model.equipo.Equipo;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.EquipoRepository;
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
import static org.mockito.Mockito.when;
/*
@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    private static final String CORREO_DOCENTE = "docente@test.com";
    private static final Long DOCENTE_ID = 99L;

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EquipoService equipoService;

    private Usuario docente;

    @BeforeEach
    void setUp() {
        docente = new Usuario(DOCENTE_ID, "Docente", CORREO_DOCENTE, "DOC", "hash", Rol.DOCENTE, null, null, null);
        when(usuarioRepository.findByCorreo(CORREO_DOCENTE)).thenReturn(Optional.of(docente));
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
        CrearEquipoRequest request = new CrearEquipoRequest(List.of(1L, 2L), 99L);

        assertThrows(IllegalArgumentException.class,
                () -> equipoService.crear(request, CORREO_DOCENTE));
    }

    @Test
    void crearEquipoRechazaEstudianteDuplicado() {
        CrearEquipoRequest request = new CrearEquipoRequest(List.of(1L, 1L), 1L);

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

    private Usuario estudiante(Long id) {
    return new Usuario(id, "Estudiante " + id, "est" + id + "@test.com",
            "E" + id, "hash", Rol.ESTUDIANTE, DOCENTE_ID, 20, "M");
}
}*/
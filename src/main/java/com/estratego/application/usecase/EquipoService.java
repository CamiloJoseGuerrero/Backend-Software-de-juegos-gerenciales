package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarEquipoRequest;
import com.estratego.application.dto.docente.CrearEquipoRequest;
import com.estratego.application.dto.docente.EquipoResponse;
import com.estratego.domain.model.equipo.Equipo;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.EquipoRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public EquipoResponse crear(CrearEquipoRequest request, String correoDocente) {
        Long docenteId = resolverDocenteId(correoDocente);
        validarEstudiantes(request.getEstudianteIds(), request.getLiderId(), docenteId);
        validarEstudiantesNoAsignados(request.getEstudianteIds(), docenteId, null);

        long siguiente = equipoRepository.countByDocenteId(docenteId) + 1;
        String nombre = "Equipo " + siguiente;

        Equipo equipo = new Equipo(
                null,
                nombre,
                docenteId,
                request.getLiderId(),
                request.getEstudianteIds()
        );

        return toResponse(equipoRepository.save(equipo));
    }

    public List<EquipoResponse> listar(String correoDocente) {
        Long docenteId = resolverDocenteId(correoDocente);
        return equipoRepository.findByDocenteId(docenteId).stream()
                .map(this::toResponse)
                .toList();
    }

    public EquipoResponse obtener(Long id, String correoDocente) {
        Long docenteId = resolverDocenteId(correoDocente);
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));
        if (!docenteId.equals(equipo.getDocenteId())) {
            throw new IllegalArgumentException("El equipo no pertenece a este docente");
        }
        return toResponse(equipo);
    }

    @Transactional
    public EquipoResponse actualizar(Long id, ActualizarEquipoRequest request, String correoDocente) {
        Long docenteId = resolverDocenteId(correoDocente);
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));

        if (!docenteId.equals(equipo.getDocenteId())) {
            throw new IllegalArgumentException("El equipo no pertenece a este docente");
        }

        validarEstudiantes(request.getEstudianteIds(), request.getLiderId(), docenteId);
        validarEstudiantesNoAsignados(request.getEstudianteIds(), docenteId, id);

        equipo.setLiderId(request.getLiderId());
        equipo.setEstudianteIds(request.getEstudianteIds());

        return toResponse(equipoRepository.save(equipo));
    }

    private void validarEstudiantes(List<Long> estudianteIds, Long liderId, Long docenteId) {
        Set<Long> idsUnicos = new HashSet<>(estudianteIds);
        if (idsUnicos.size() != estudianteIds.size()) {
            throw new IllegalArgumentException("No se pueden repetir estudiantes en el equipo");
        }

        if (!estudianteIds.contains(liderId)) {
            throw new IllegalArgumentException("El líder debe ser uno de los estudiantes del equipo");
        }

        for (Long id : estudianteIds) {
            Usuario estudiante = usuarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + id));

            if (estudiante.getRol() != Rol.ESTUDIANTE) {
                throw new IllegalArgumentException("El usuario " + id + " no es estudiante");
            }
            if (!docenteId.equals(estudiante.getDocenteId())) {
                throw new IllegalArgumentException("El estudiante " + id + " no pertenece a este docente");
            }
        }
    }

    private void validarEstudiantesNoAsignados(List<Long> estudianteIds, Long docenteId, Long equipoIdExcluir) {
        List<Equipo> equipos = equipoRepository.findByDocenteId(docenteId);
        for (Equipo equipo : equipos) {
            if (equipoIdExcluir != null && equipo.getId().equals(equipoIdExcluir)) {
                continue;
            }
            for (Long estudianteId : estudianteIds) {
                if (equipo.getEstudianteIds().contains(estudianteId)) {
                    throw new IllegalArgumentException(
                            "El estudiante " + estudianteId + " ya está en el equipo " + equipo.getNombre()
                    );
                }
            }
        }
    }

    private Long resolverDocenteId(String correoDocente) {
        return usuarioRepository.findByCorreo(correoDocente)
                .orElseThrow(() -> new InvalidCredentialsException("Docente no encontrado"))
                .getId();
    }

    private EquipoResponse toResponse(Equipo equipo) {
        return new EquipoResponse(
                equipo.getId(),
                equipo.getNombre(),
                equipo.getDocenteId(),
                equipo.getLiderId(),
                equipo.getEstudianteIds()
        );
    }
}
package com.estratego.application.usecase;

import com.estratego.application.dto.docente.EstudianteResponse;
import com.estratego.domain.model.usuario.Estudiante;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.CursoEstudianteRepository;
import com.estratego.domain.repository.CursoRepository;
import com.estratego.domain.repository.DocenteEstudianteRepository;
import com.estratego.domain.repository.EstudianteRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocenteEstudianteService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final DocenteEstudianteRepository docenteEstudianteRepository;
    private final CursoRepository cursoRepository;
    private final CursoEstudianteRepository cursoEstudianteRepository;

    public List<EstudianteResponse> listarEstudiantesDelDocente(String correoDocente) {
        return listarEstudiantesDelDocente(correoDocente, null);
    }

    public List<EstudianteResponse> listarEstudiantesDelDocente(String correoDocente, Long idCurso) {
        Long idDocente = resolverDocenteId(correoDocente);
        List<Long> ids;
        if (idCurso != null) {
            var curso = cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
            if (!idDocente.equals(curso.getIdDocente())) {
                throw new IllegalArgumentException("El curso no pertenece a este docente");
            }
            ids = cursoEstudianteRepository.findIdsEstudiantes(idCurso);
        } else {
            ids = docenteEstudianteRepository.findIdsEstudiantes(idDocente);
        }
        return ids.stream()
                .map(usuarioRepository::findById)
                .flatMap(Optional::stream)
                .filter(u -> u.getRol() == Rol.ESTUDIANTE)
                .map(this::toResponse)
                .toList();
    }

    public EstudianteResponse obtenerEstudiante(Long id, String correoDocente) {
        Long idDocente = resolverDocenteId(correoDocente);

        if (!docenteEstudianteRepository.existeVinculo(idDocente, id)) {
            throw new IllegalArgumentException("Estudiante no encontrado");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getRol() == Rol.ESTUDIANTE)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        return toResponse(usuario);
    }

    private Long resolverDocenteId(String correoDocente) {
        return usuarioRepository.findByCorreo(correoDocente)
                .orElseThrow(() -> new InvalidCredentialsException("Docente no encontrado"))
                .getId();
    }

    private EstudianteResponse toResponse(Usuario u) {
        Estudiante estudiante = estudianteRepository.findById(u.getId()).orElse(null);

        return new EstudianteResponse(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getNumeroIdentificacion(),
                estudiante != null ? estudiante.getEdad() : null,
                estudiante != null ? estudiante.getGenero() : null,
                u.getUsuario()
        );
    }
}

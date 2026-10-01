package com.estratego.application.usecase;

import com.estratego.application.dto.docente.EstudianteResponse;
import com.estratego.domain.model.usuario.Estudiante;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.DocenteEstudianteRepository;
import com.estratego.domain.repository.EstudianteRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Consulta de estudiantes: cada docente ve solo los que él cargó. */
@Service
@RequiredArgsConstructor
public class DocenteEstudianteService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final DocenteEstudianteRepository docenteEstudianteRepository;

    public List<EstudianteResponse> listarEstudiantesDelDocente(String correoDocente) {
        Long idDocente = resolverDocenteId(correoDocente);
        return docenteEstudianteRepository.findIdsEstudiantes(idDocente).stream()
                .map(usuarioRepository::findById)
                .flatMap(Optional::stream)
                .filter(u -> u.getRol() == Rol.ESTUDIANTE)
                .map(this::toResponse)
                .toList();
    }

    public EstudianteResponse obtenerEstudiante(Long id, String correoDocente) {
        Long idDocente = resolverDocenteId(correoDocente);

        // Mismo mensaje si no existe o si es de otro docente: no revela estudiantes ajenos
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

package com.estratego.application.usecase;

import com.estratego.application.dto.docente.EstudianteResponse;
import com.estratego.domain.model.usuario.Estudiante;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.EstudianteRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocenteEstudianteService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;

    public List<EstudianteResponse> listarTodosLosEstudiantes() {
        return usuarioRepository.findByRol(Rol.ESTUDIANTE).stream()
                .map(this::toResponse)
                .toList();
    }

    public EstudianteResponse obtenerEstudiante(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        if (usuario.getRol() != Rol.ESTUDIANTE) {
            throw new IllegalArgumentException("El usuario no es un estudiante");
        }

        return toResponse(usuario);
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
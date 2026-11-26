package com.estratego.application.usecase;

import com.estratego.application.dto.docente.CrearCursoRequest;
import com.estratego.application.dto.docente.CursoResponse;
import com.estratego.application.dto.docente.EstudianteResponse;
import com.estratego.application.dto.docente.SimulacionResponse;
import com.estratego.domain.model.curso.Curso;
import com.estratego.domain.model.usuario.Estudiante;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.CursoEstudianteRepository;
import com.estratego.domain.repository.CursoRepository;
import com.estratego.domain.repository.DocenteEstudianteRepository;
import com.estratego.domain.repository.EstudianteRepository;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;
    private final CursoEstudianteRepository cursoEstudianteRepository;
    private final DocenteEstudianteRepository docenteEstudianteRepository;
    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;

    public List<CursoResponse> listar(String correoDocente) {
        Long idDocente = resolverDocenteId(correoDocente);
        return cursoRepository.findByIdDocente(idDocente).stream()
                .map(this::toResponse)
                .toList();
    }

    public CursoResponse obtener(Long idCurso, String correoDocente) {
        return toResponse(obtenerYValidar(idCurso, correoDocente));
    }

    @Transactional
    public CursoResponse crear(CrearCursoRequest request, String correoDocente) {
        Long idDocente = resolverDocenteId(correoDocente);
        Curso creado = cursoRepository.save(
                new Curso(null, idDocente, request.getNombre().trim(), LocalDateTime.now())
        );
        return toResponse(creado);
    }

    @Transactional
    public CursoResponse actualizar(Long idCurso, CrearCursoRequest request, String correoDocente) {
        Curso curso = obtenerYValidar(idCurso, correoDocente);
        curso.setNombre(request.getNombre().trim());
        return toResponse(cursoRepository.save(curso));
    }

    @Transactional
    public void eliminar(Long idCurso, String correoDocente) {
        obtenerYValidar(idCurso, correoDocente);
        cursoRepository.deleteById(idCurso);
    }

    public List<EstudianteResponse> listarEstudiantes(Long idCurso, String correoDocente) {
        obtenerYValidar(idCurso, correoDocente);
        return cursoEstudianteRepository.findIdsEstudiantes(idCurso).stream()
                .map(usuarioRepository::findById)
                .flatMap(Optional::stream)
                .filter(u -> u.getRol() == Rol.ESTUDIANTE)
                .map(this::toEstudianteResponse)
                .toList();
    }

    @Transactional
    public void agregarEstudiante(Long idCurso, Long idEstudiante, String correoDocente) {
        Curso curso = obtenerYValidar(idCurso, correoDocente);
        if (!docenteEstudianteRepository.existeVinculo(curso.getIdDocente(), idEstudiante)) {
            throw new IllegalArgumentException("El estudiante no está en tu lista de estudiantes");
        }
        cursoEstudianteRepository.vincular(idCurso, idEstudiante);
    }

    @Transactional
    public void quitarEstudiante(Long idCurso, Long idEstudiante, String correoDocente) {
        obtenerYValidar(idCurso, correoDocente);
        cursoEstudianteRepository.desvincular(idCurso, idEstudiante);
    }

    public List<SimulacionResponse> listarSimulaciones(Long idCurso, String correoDocente) {
        Curso curso = obtenerYValidar(idCurso, correoDocente);
        return simulacionRepository.findByIdUsuarioCoordinadorAndIdCurso(curso.getIdDocente(), idCurso).stream()
                .map(s -> new SimulacionResponse(
                        s.getId(),
                        s.getIdUsuarioCoordinador(),
                        s.getNombre(),
                        s.getFechaInicio(),
                        s.getFechaFin(),
                        s.getEstado(),
                        s.getIdCurso()
                ))
                .toList();
    }

    public Curso obtenerYValidar(Long idCurso, String correoDocente) {
        Long idDocente = resolverDocenteId(correoDocente);
        return obtenerYValidarPorDocente(idCurso, idDocente);
    }

    public Curso obtenerYValidarPorDocente(Long idCurso, Long idDocente) {
        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        if (!idDocente.equals(curso.getIdDocente())) {
            throw new IllegalArgumentException("El curso no pertenece a este docente");
        }
        return curso;
    }

    private Long resolverDocenteId(String correoDocente) {
        return usuarioRepository.findByCorreo(correoDocente)
                .orElseThrow(() -> new InvalidCredentialsException("Docente no encontrado"))
                .getId();
    }

    private CursoResponse toResponse(Curso c) {
        return new CursoResponse(
                c.getId(),
                c.getNombre(),
                c.getFechaCreacion(),
                cursoEstudianteRepository.countByIdCurso(c.getId()),
                simulacionRepository.countByIdCurso(c.getId())
        );
    }

    private EstudianteResponse toEstudianteResponse(Usuario u) {
        Estudiante est = estudianteRepository.findById(u.getId()).orElse(null);
        return new EstudianteResponse(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getNumeroIdentificacion(),
                est != null ? est.getEdad() : null,
                est != null ? est.getGenero() : null,
                u.getUsuario()
        );
    }
}

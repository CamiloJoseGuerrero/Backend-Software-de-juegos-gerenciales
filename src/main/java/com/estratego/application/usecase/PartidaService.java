package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarPartidaRequest;
import com.estratego.application.dto.docente.CrearPartidaRequest;
import com.estratego.application.dto.docente.PartidaResponse;
import com.estratego.domain.model.partida.EstadoPartida;
import com.estratego.domain.model.partida.Partida;
import com.estratego.domain.repository.PartidaRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.estratego.application.dto.docente.AsignarEquiposRequest;
import com.estratego.application.dto.docente.EquipoResponse;
import com.estratego.application.dto.docente.PartidaEquiposResponse;
import com.estratego.domain.model.equipo.Equipo;
import com.estratego.domain.repository.EquipoRepository;
import java.util.HashSet;
import java.util.Set;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;

    @Transactional
    public PartidaResponse crear(CrearPartidaRequest request, String correoDocente) {
        Long docenteId = resolverDocenteId(correoDocente);
        validarFechas(request.getFechaHoraInicio(), request.getDuracionMinutos(), request.getFechaVisualizacion());

        LocalDateTime fechaCierre = request.getFechaHoraInicio().plusMinutes(request.getDuracionMinutos());

        Partida partida = new Partida(
                null,
                request.getCasoId(),
                docenteId,
                request.getFechaHoraInicio(),
                fechaCierre,
                request.getDuracionMinutos(),
                request.getFechaVisualizacion(),
                EstadoPartida.CONFIGURADA,
                new ArrayList<>()
        );

        return toResponse(partidaRepository.save(partida));
    }

    public List<PartidaResponse> listar(String correoDocente) {
        Long docenteId = resolverDocenteId(correoDocente);
        return partidaRepository.findByDocenteId(docenteId).stream()
                .map(this::toResponse)
                .toList();
    }

    public PartidaResponse obtener(Long id, String correoDocente) {
        return toResponse(obtenerYValidar(id, correoDocente));
    }

    @Transactional
    public PartidaResponse actualizar(Long id, ActualizarPartidaRequest request, String correoDocente) {
        Partida partida = obtenerYValidar(id, correoDocente);

        if (partida.getEstado() == EstadoPartida.EN_CURSO || partida.getEstado() == EstadoPartida.FINALIZADA) {
            throw new IllegalArgumentException("No se puede editar una partida en curso o finalizada");
        }

        validarFechas(request.getFechaHoraInicio(), request.getDuracionMinutos(), request.getFechaVisualizacion());

        LocalDateTime fechaCierre = request.getFechaHoraInicio().plusMinutes(request.getDuracionMinutos());

        partida.setFechaHoraInicio(request.getFechaHoraInicio());
        partida.setDuracionMinutos(request.getDuracionMinutos());
        partida.setFechaHoraCierre(fechaCierre);
        partida.setFechaVisualizacion(request.getFechaVisualizacion());

        return toResponse(partidaRepository.save(partida));
    }

    @Transactional
    public PartidaResponse programar(Long id, String correoDocente) {
        Partida partida = obtenerYValidar(id, correoDocente);
        if (partida.getEstado() != EstadoPartida.CONFIGURADA) {
            throw new IllegalArgumentException("Solo se puede programar una partida en estado CONFIGURADA");
        }
        partida.setEstado(EstadoPartida.PROGRAMADA);
        return toResponse(partidaRepository.save(partida));
    }

    @Transactional
    public PartidaResponse iniciar(Long id, String correoDocente) {
        Partida partida = obtenerYValidar(id, correoDocente);
        if (partida.getEstado() != EstadoPartida.PROGRAMADA) {
            throw new IllegalArgumentException("Solo se puede iniciar una partida en estado PROGRAMADA");
        }
        if (LocalDateTime.now().isBefore(partida.getFechaHoraInicio())) {
            throw new IllegalArgumentException("Aún no ha llegado la fecha de inicio");
        }
        partida.setEstado(EstadoPartida.EN_CURSO);
        return toResponse(partidaRepository.save(partida));
    }

    @Transactional
    public PartidaResponse finalizar(Long id, String correoDocente) {
        Partida partida = obtenerYValidar(id, correoDocente);
        if (partida.getEstado() != EstadoPartida.EN_CURSO) {
            throw new IllegalArgumentException("Solo se puede finalizar una partida en estado EN_CURSO");
        }
        partida.setEstado(EstadoPartida.FINALIZADA);
        return toResponse(partidaRepository.save(partida));
    }

    @Transactional
    public void eliminar(Long id, String correoDocente) {
        Partida partida = obtenerYValidar(id, correoDocente);
        if (partida.getEstado() != EstadoPartida.CONFIGURADA) {
            throw new IllegalArgumentException("Solo se puede eliminar una partida en estado CONFIGURADA");
        }
        partidaRepository.deleteById(id);
    }

    private void validarFechas(LocalDateTime fechaInicio, Integer duracion, LocalDateTime fechaVisualizacion) {
        if (fechaInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser futura");
        }
        if (duracion == null || duracion <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a 0");
        }
        if (fechaVisualizacion != null && fechaVisualizacion.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de visualización no puede ser anterior al inicio");
        }
    }

    private Partida obtenerYValidar(Long id, String correoDocente) {
        Long docenteId = resolverDocenteId(correoDocente);
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));
        if (!docenteId.equals(partida.getDocenteId())) {
            throw new IllegalArgumentException("La partida no pertenece a este docente");
        }
        return partida;
    }

    private Long resolverDocenteId(String correoDocente) {
        return usuarioRepository.findByCorreo(correoDocente)
                .orElseThrow(() -> new InvalidCredentialsException("Docente no encontrado"))
                .getId();
    }

    private PartidaResponse toResponse(Partida p) {
        return new PartidaResponse(
                p.getId(),
                p.getCasoId(),
                p.getDocenteId(),
                p.getFechaHoraInicio(),
                p.getFechaHoraCierre(),
                p.getDuracionMinutos(),
                p.getFechaVisualizacion(),
                p.getEstado(),
                p.getEquipoIds()
        );
    }

    @Transactional
public PartidaEquiposResponse asignarEquipos(Long partidaId, AsignarEquiposRequest request, String correoDocente) {
    Partida partida = obtenerYValidar(partidaId, correoDocente);

    if (partida.getEstado() != EstadoPartida.CONFIGURADA
            && partida.getEstado() != EstadoPartida.PROGRAMADA) {
        throw new IllegalArgumentException(
                "Solo se pueden asignar equipos en estado CONFIGURADA o PROGRAMADA");
    }

    // Validar que los equipos existan y pertenezcan al docente
    List<Equipo> equipos = equipoRepository.findByIdIn(request.getEquipoIds());
    if (equipos.size() != request.getEquipoIds().size()) {
        throw new IllegalArgumentException("Uno o más equipos no existen");
    }
    for (Equipo equipo : equipos) {
        if (!partida.getDocenteId().equals(equipo.getDocenteId())) {
            throw new IllegalArgumentException(
                    "El equipo " + equipo.getId() + " no pertenece a este docente");
        }
    }

    // Validar que ningún equipo esté en otra partida EN_CURSO
    for (Long equipoId : request.getEquipoIds()) {
        if (partidaRepository.existeEquipoEnPartidaEnCurso(equipoId, partidaId)) {
            throw new IllegalArgumentException(
                    "El equipo " + equipoId + " ya está en otra partida en curso");
        }
    }

    // Asignar sin duplicar
    Set<Long> actuales = new HashSet<>(partida.getEquipoIds());
    for (Long equipoId : request.getEquipoIds()) {
        actuales.add(equipoId);
    }
    partida.setEquipoIds(new ArrayList<>(actuales));

    partidaRepository.save(partida);

    return toEquiposResponse(partida, equipos);
}

@Transactional
public PartidaEquiposResponse quitarEquipo(Long partidaId, Long equipoId, String correoDocente) {
    Partida partida = obtenerYValidar(partidaId, correoDocente);

    if (!partida.getEquipoIds().contains(equipoId)) {
        throw new IllegalArgumentException("El equipo no está asignado a esta partida");
    }

    partida.getEquipoIds().remove(equipoId);
    partidaRepository.save(partida);

    List<Equipo> equipos = equipoRepository.findByIdIn(partida.getEquipoIds());
    return toEquiposResponse(partida, equipos);
}

public PartidaEquiposResponse listarEquipos(Long partidaId, String correoDocente) {
    Partida partida = obtenerYValidar(partidaId, correoDocente);
    List<Equipo> equipos = equipoRepository.findByIdIn(partida.getEquipoIds());
    return toEquiposResponse(partida, equipos);
}

private PartidaEquiposResponse toEquiposResponse(Partida partida, List<Equipo> equipos) {
    List<EquipoResponse> responses = equipos.stream()
            .map(e -> new EquipoResponse(
                    e.getId(),
                    e.getNombre(),
                    e.getDocenteId(),
                    e.getLiderId(),
                    e.getEstudianteIds()))
            .toList();

    return new PartidaEquiposResponse(partida.getId(), responses);
}

}
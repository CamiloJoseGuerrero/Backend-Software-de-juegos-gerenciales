package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarSimulacionRequest;
import com.estratego.application.dto.docente.CrearSimulacionRequest;
import com.estratego.application.dto.docente.SimulacionResponse;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulacionService {

    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public SimulacionResponse crear(CrearSimulacionRequest request, String correoCoordinador) {
        Long idCoordinador = resolverCoordinadorId(correoCoordinador);

        validarFechas(request.getFechaInicio(), request.getFechaFin());

        Simulacion simulacion = new Simulacion(
                null,
                idCoordinador,
                request.getNombre().trim(),
                request.getFechaInicio(),
                request.getFechaFin(),
                EstadoSimulacion.BORRADOR
        );

        return toResponse(simulacionRepository.save(simulacion));
    }

    public List<SimulacionResponse> listar(String correoCoordinador) {
        Long idCoordinador = resolverCoordinadorId(correoCoordinador);
        return simulacionRepository.findByIdUsuarioCoordinador(idCoordinador).stream()
                .map(this::toResponse)
                .toList();
    }

    public SimulacionResponse obtener(Long id, String correoCoordinador) {
        return toResponse(obtenerYValidar(id, correoCoordinador));
    }

    @Transactional
    public SimulacionResponse actualizar(Long id, ActualizarSimulacionRequest request, String correoCoordinador) {
        Simulacion simulacion = obtenerYValidar(id, correoCoordinador);

        if (simulacion.getEstado() == EstadoSimulacion.EN_CURSO
                || simulacion.getEstado() == EstadoSimulacion.FINALIZADA) {
            throw new IllegalArgumentException("No se puede editar una simulación en curso o finalizada");
        }

        validarFechas(request.getFechaInicio(), request.getFechaFin());

        simulacion.setNombre(request.getNombre().trim());
        simulacion.setFechaInicio(request.getFechaInicio());
        simulacion.setFechaFin(request.getFechaFin());

        return toResponse(simulacionRepository.save(simulacion));
    }

    @Transactional
    public SimulacionResponse programar(Long id, String correoCoordinador) {
        Simulacion simulacion = obtenerYValidar(id, correoCoordinador);

        if (simulacion.getEstado() != EstadoSimulacion.BORRADOR) {
            throw new IllegalArgumentException("Solo se puede programar una simulación en BORRADOR");
        }

        simulacion.setEstado(EstadoSimulacion.PROGRAMADA);
        return toResponse(simulacionRepository.save(simulacion));
    }

    @Transactional
    public SimulacionResponse iniciar(Long id, String correoCoordinador) {
        Simulacion simulacion = obtenerYValidar(id, correoCoordinador);

        if (simulacion.getEstado() != EstadoSimulacion.PROGRAMADA) {
            throw new IllegalArgumentException("Solo se puede iniciar una simulación en PROGRAMADA");
        }

        if (simulacion.getFechaInicio().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Aún no ha llegado la fecha de inicio");
        }

        simulacion.setEstado(EstadoSimulacion.EN_CURSO);
        return toResponse(simulacionRepository.save(simulacion));
    }

    @Transactional
    public SimulacionResponse finalizar(Long id, String correoCoordinador) {
        Simulacion simulacion = obtenerYValidar(id, correoCoordinador);

        if (simulacion.getEstado() != EstadoSimulacion.EN_CURSO) {
            throw new IllegalArgumentException("Solo se puede finalizar una simulación EN_CURSO");
        }

        simulacion.setEstado(EstadoSimulacion.FINALIZADA);
        return toResponse(simulacionRepository.save(simulacion));
    }

    @Transactional
    public void eliminar(Long id, String correoCoordinador) {
        Simulacion simulacion = obtenerYValidar(id, correoCoordinador);

        if (simulacion.getEstado() != EstadoSimulacion.BORRADOR) {
            throw new IllegalArgumentException("Solo se puede eliminar una simulación en BORRADOR");
        }

        simulacionRepository.deleteById(id);
    }

    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
        }
    }

    private Simulacion obtenerYValidar(Long id, String correoCoordinador) {
        Long idCoordinador = resolverCoordinadorId(correoCoordinador);

        Simulacion simulacion = simulacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        if (!idCoordinador.equals(simulacion.getIdUsuarioCoordinador())) {
            throw new IllegalArgumentException("La simulación no pertenece a este coordinador");
        }

        return simulacion;
    }

    private Long resolverCoordinadorId(String correoCoordinador) {
        return usuarioRepository.findByCorreo(correoCoordinador)
                .orElseThrow(() -> new InvalidCredentialsException("Coordinador no encontrado"))
                .getId();
    }

    private SimulacionResponse toResponse(Simulacion s) {
        return new SimulacionResponse(
                s.getId(),
                s.getIdUsuarioCoordinador(),
                s.getNombre(),
                s.getFechaInicio(),
                s.getFechaFin(),
                s.getEstado()
        );
    }
}
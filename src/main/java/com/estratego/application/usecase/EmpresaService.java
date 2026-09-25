package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarEmpresaRequest;
import com.estratego.application.dto.docente.CrearEmpresaRequest;
import com.estratego.application.dto.docente.EmpresaResponse;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.EmpresaRepository;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public EmpresaResponse crear(Long idSimulacion, CrearEmpresaRequest request, String correoDocente) {
        Simulacion simulacion = obtenerSimulacionYValidar(idSimulacion, correoDocente);

        if (simulacion.getEstado() != EstadoSimulacion.BORRADOR
                && simulacion.getEstado() != EstadoSimulacion.PROGRAMADA) {
            throw new IllegalArgumentException(
                    "Solo se pueden crear empresas en simulaciones BORRADOR o PROGRAMADA");
        }

        long siguiente = empresaRepository.countByIdSimulacion(idSimulacion) + 1;
        String codigo = String.format("EMP-%03d", siguiente);

        Empresa empresa = new Empresa(
                null,
                idSimulacion,
                codigo,
                request.getNombre().trim(),
                request.getEstrategia(),
                request.getTipoJugador() != null ? request.getTipoJugador() : TipoJugador.MULTIUSUARIO,
                EstadoEmpresa.ACTIVA
        );

        return toResponse(empresaRepository.save(empresa));
    }

    public List<EmpresaResponse> listarPorSimulacion(Long idSimulacion, String correoDocente) {
        obtenerSimulacionYValidar(idSimulacion, correoDocente);

        return empresaRepository.findByIdSimulacion(idSimulacion).stream()
                .map(this::toResponse)
                .toList();
    }

    public EmpresaResponse obtener(Long id, String correoDocente) {
        Empresa empresa = obtenerYValidar(id, correoDocente);
        return toResponse(empresa);
    }

    @Transactional
    public EmpresaResponse actualizar(Long id, ActualizarEmpresaRequest request, String correoDocente) {
        Empresa empresa = obtenerYValidar(id, correoDocente);

        Simulacion simulacion = simulacionRepository.findById(empresa.getIdSimulacion())
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        if (simulacion.getEstado() == EstadoSimulacion.EN_CURSO
                || simulacion.getEstado() == EstadoSimulacion.FINALIZADA) {
            throw new IllegalArgumentException(
                    "No se pueden editar empresas de simulaciones en curso o finalizadas");
        }

        empresa.setNombre(request.getNombre().trim());
        empresa.setEstrategia(request.getEstrategia());
        if (request.getTipoJugador() != null) {
            empresa.setTipoJugador(request.getTipoJugador());
        }

        return toResponse(empresaRepository.save(empresa));
    }

    @Transactional
    public void eliminar(Long id, String correoDocente) {
        Empresa empresa = obtenerYValidar(id, correoDocente);

        Simulacion simulacion = simulacionRepository.findById(empresa.getIdSimulacion())
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        if (simulacion.getEstado() != EstadoSimulacion.BORRADOR
                && simulacion.getEstado() != EstadoSimulacion.PROGRAMADA) {
            throw new IllegalArgumentException(
                    "Solo se pueden eliminar empresas de simulaciones BORRADOR o PROGRAMADA");
        }

        empresaRepository.deleteById(id);
    }

    private Empresa obtenerYValidar(Long id, String correoDocente) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Simulacion simulacion = simulacionRepository.findById(empresa.getIdSimulacion())
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        Long idDocente = resolverDocenteId(correoDocente);

        if (!idDocente.equals(simulacion.getIdUsuarioCoordinador())) {
            throw new IllegalArgumentException("La empresa no pertenece a una simulación de este docente");
        }

        return empresa;
    }

    private Simulacion obtenerSimulacionYValidar(Long idSimulacion, String correoDocente) {
        Long idDocente = resolverDocenteId(correoDocente);

        Simulacion simulacion = simulacionRepository.findById(idSimulacion)
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        if (!idDocente.equals(simulacion.getIdUsuarioCoordinador())) {
            throw new IllegalArgumentException("La simulación no pertenece a este docente");
        }

        return simulacion;
    }

    private Long resolverDocenteId(String correoDocente) {
        return usuarioRepository.findByCorreo(correoDocente)
                .orElseThrow(() -> new InvalidCredentialsException("Docente no encontrado"))
                .getId();
    }

    private EmpresaResponse toResponse(Empresa e) {
        return new EmpresaResponse(
                e.getId(),
                e.getIdSimulacion(),
                e.getCodigoEmpresa(),
                e.getNombre(),
                e.getEstrategia(),
                e.getTipoJugador(),
                e.getEstado()
        );
    }
}
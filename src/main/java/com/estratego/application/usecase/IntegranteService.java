package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarIntegranteRequest;
import com.estratego.application.dto.docente.AgregarIntegranteRequest;
import com.estratego.application.dto.docente.IntegranteResponse;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.empresa.TipoJugador;
import com.estratego.domain.model.integrante.Departamento;
import com.estratego.domain.model.integrante.Integrante;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.model.usuario.Rol;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.DocenteEstudianteRepository;
import com.estratego.domain.repository.EmpresaRepository;
import com.estratego.domain.repository.IntegranteRepository;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegranteService {

    private final IntegranteRepository integranteRepository;
    private final EmpresaRepository empresaRepository;
    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final DocenteEstudianteRepository docenteEstudianteRepository;

    @Transactional
    public IntegranteResponse agregar(Long idEmpresa, AgregarIntegranteRequest request, String correoDocente) {
        Contexto ctx = obtenerYValidar(idEmpresa, correoDocente);
        validarSimulacionEditable(ctx.simulacion());

        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        if (usuario.getRol() != Rol.ESTUDIANTE) {
            throw new IllegalArgumentException("El usuario no es un estudiante");
        }
        if (!docenteEstudianteRepository.existeVinculo(
                ctx.simulacion().getIdUsuarioCoordinador(), usuario.getId())) {
            throw new IllegalArgumentException("El estudiante no está en tu lista de estudiantes");
        }

        if (integranteRepository.existsEnSimulacion(ctx.simulacion().getId(), usuario.getId())) {
            throw new IllegalArgumentException(
                    "El estudiante ya pertenece a una empresa de esta simulación");
        }

        boolean monousuario = ctx.empresa().getTipoJugador() == TipoJugador.MONOUSUARIO;
        if (monousuario && integranteRepository.countByIdEmpresa(idEmpresa) >= 1) {
            throw new IllegalArgumentException("Una empresa MONOUSUARIO solo puede tener un integrante");
        }

        // En MONOUSUARIO el único integrante es el líder
        boolean esLider = monousuario || Boolean.TRUE.equals(request.getEsLider());
        if (esLider) {
            integranteRepository.quitarLider(idEmpresa);
        }

        Departamento departamento = request.getDepartamento() != null
                ? request.getDepartamento()
                : Departamento.GERENCIA_GENERAL;

        Integrante guardado = integranteRepository.save(
                new Integrante(null, idEmpresa, usuario.getId(), departamento, esLider));

        return toResponse(guardado, usuario);
    }

    public List<IntegranteResponse> listar(Long idEmpresa, String correoDocente) {
        obtenerYValidar(idEmpresa, correoDocente);

        return integranteRepository.findByIdEmpresa(idEmpresa).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public IntegranteResponse actualizar(Long idEmpresa, Long idUsuario,
                                         ActualizarIntegranteRequest request, String correoDocente) {
        Contexto ctx = obtenerYValidar(idEmpresa, correoDocente);
        validarSimulacionEditable(ctx.simulacion());

        Integrante integrante = buscarIntegrante(idEmpresa, idUsuario);
        integrante.setDepartamento(request.getDepartamento());

        return toResponse(integranteRepository.save(integrante));
    }

    @Transactional
    public IntegranteResponse asignarLider(Long idEmpresa, Long idUsuario, String correoDocente) {
        Contexto ctx = obtenerYValidar(idEmpresa, correoDocente);
        validarSimulacionEditable(ctx.simulacion());

        Integrante integrante = buscarIntegrante(idEmpresa, idUsuario);
        if (integrante.isEsLider()) {
            return toResponse(integrante);
        }

        integranteRepository.quitarLider(idEmpresa);
        integrante.setEsLider(true);

        return toResponse(integranteRepository.save(integrante));
    }

    @Transactional
    public void quitar(Long idEmpresa, Long idUsuario, String correoDocente) {
        Contexto ctx = obtenerYValidar(idEmpresa, correoDocente);
        validarSimulacionEditable(ctx.simulacion());

        Integrante integrante = buscarIntegrante(idEmpresa, idUsuario);
        integranteRepository.deleteById(integrante.getId());
    }

    private Integrante buscarIntegrante(Long idEmpresa, Long idUsuario) {
        return integranteRepository.findByIdEmpresaAndIdUsuario(idEmpresa, idUsuario)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El estudiante no es integrante de esta empresa"));
    }

    private void validarSimulacionEditable(Simulacion simulacion) {
        if (simulacion.getEstado() != EstadoSimulacion.BORRADOR
                && simulacion.getEstado() != EstadoSimulacion.PROGRAMADA) {
            throw new IllegalArgumentException(
                    "Solo se pueden modificar integrantes en simulaciones BORRADOR o PROGRAMADA");
        }
    }

    private Contexto obtenerYValidar(Long idEmpresa, String correoDocente) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Simulacion simulacion = simulacionRepository.findById(empresa.getIdSimulacion())
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        Long idDocente = usuarioRepository.findByCorreo(correoDocente)
                .orElseThrow(() -> new InvalidCredentialsException("Docente no encontrado"))
                .getId();

        if (!idDocente.equals(simulacion.getIdUsuarioCoordinador())) {
            throw new IllegalArgumentException("La empresa no pertenece a una simulación de este docente");
        }

        return new Contexto(empresa, simulacion);
    }

    private IntegranteResponse toResponse(Integrante i) {
        Usuario u = usuarioRepository.findById(i.getIdUsuario()).orElse(null);
        return toResponse(i, u);
    }

    private IntegranteResponse toResponse(Integrante i, Usuario u) {
        return new IntegranteResponse(
                i.getId(),
                i.getIdEmpresa(),
                i.getIdUsuario(),
                u != null ? u.getNombre() : null,
                u != null ? u.getCorreo() : null,
                u != null ? u.getNumeroIdentificacion() : null,
                i.getDepartamento(),
                i.isEsLider()
        );
    }

    private record Contexto(Empresa empresa, Simulacion simulacion) {
    }
}

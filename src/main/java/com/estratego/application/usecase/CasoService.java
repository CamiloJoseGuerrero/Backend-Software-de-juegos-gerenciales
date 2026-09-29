package com.estratego.application.usecase;

import com.estratego.application.dto.docente.*;
import com.estratego.domain.model.caso.*;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CasoService {

    private final CasoRepository casoRepository;
    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final DecisionCasoRepository decisionCasoRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public CasoResponse crear(Long idSimulacion, CasoRequest request, String correoDocente) {
        if (idSimulacion == null) {
            throw new IllegalArgumentException("Debe indicar la simulación (idSimulacion)");
        }
        Simulacion simulacion = obtenerSimulacionYValidar(idSimulacion, correoDocente);
        validarSimulacionEditable(simulacion);
        validarDatos(request, simulacion);

        Caso caso = new Caso();
        caso.setIdSimulacion(idSimulacion);
        caso.setEstado(EstadoCaso.BORRADOR);
        copiar(request, caso);

        return toResponse(casoRepository.save(caso));
    }

    /** Todos los casos de todas las simulaciones del docente (pestaña "Casos"). */
    public List<CasoResponse> listarDelDocente(String correoDocente) {
        Long idDocente = resolverDocenteId(correoDocente);
        List<Long> idsSimulacion = simulacionRepository.findByIdUsuarioCoordinador(idDocente).stream()
                .map(Simulacion::getId)
                .toList();
        return casoRepository.findByIdSimulacionIn(idsSimulacion).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CasoResponse> listarPorSimulacion(Long idSimulacion, String correoDocente) {
        obtenerSimulacionYValidar(idSimulacion, correoDocente);
        return casoRepository.findByIdSimulacion(idSimulacion).stream()
                .map(this::toResponse)
                .toList();
    }

    public CasoResponse obtener(Long id, String correoDocente) {
        return toResponse(obtenerYValidar(id, correoDocente).caso());
    }

    @Transactional
    public CasoResponse actualizar(Long id, CasoRequest request, String correoDocente) {
        Contexto ctx = obtenerYValidar(id, correoDocente);
        validarSimulacionEditable(ctx.simulacion());
        validarSinDecisiones(id);
        validarDatos(request, ctx.simulacion());

        Caso caso = ctx.caso();
        copiar(request, caso);

        return toResponse(casoRepository.save(caso));
    }

    /** Deja este caso como el ACTIVO de su simulación; el que estaba activo pasa a BORRADOR. */
    @Transactional
    public CasoResponse activar(Long id, String correoDocente) {
        Contexto ctx = obtenerYValidar(id, correoDocente);
        if (ctx.simulacion().getEstado() == EstadoSimulacion.FINALIZADA) {
            throw new IllegalArgumentException("No se pueden activar casos de una simulación finalizada");
        }

        Caso caso = ctx.caso();
        if (caso.getEstado() == EstadoCaso.ACTIVO) {
            return toResponse(caso);
        }

        // Solo cambia el estado: no toca las opciones (sus ids pueden tener decisiones asociadas)
        casoRepository.desactivarTodos(caso.getIdSimulacion());
        casoRepository.marcarActivo(id);
        caso.setEstado(EstadoCaso.ACTIVO);
        return toResponse(caso);
    }

    @Transactional
    public void eliminar(Long id, String correoDocente) {
        Contexto ctx = obtenerYValidar(id, correoDocente);
        validarSimulacionEditable(ctx.simulacion());
        validarSinDecisiones(id);
        casoRepository.deleteById(id);
    }

    /** Qué eligió cada empresa de la simulación en este caso. */
    public List<DecisionEmpresaResponse> decisiones(Long id, String correoDocente) {
        Contexto ctx = obtenerYValidar(id, correoDocente);
        Caso caso = ctx.caso();

        Map<Long, DecisionCaso> porEmpresa = decisionCasoRepository.findByIdCaso(id).stream()
                .collect(Collectors.toMap(DecisionCaso::getIdEmpresa, Function.identity()));
        Map<Long, OpcionCaso> opciones = caso.getOpciones().stream()
                .collect(Collectors.toMap(OpcionCaso::getId, Function.identity()));

        List<DecisionEmpresaResponse> resultado = new ArrayList<>();
        for (Empresa e : empresaRepository.findByIdSimulacion(caso.getIdSimulacion())) {
            DecisionCaso d = porEmpresa.get(e.getId());
            if (d == null) {
                resultado.add(new DecisionEmpresaResponse(e.getId(), e.getCodigoEmpresa(), e.getNombre(),
                        false, null, null, null, null, null));
                continue;
            }
            OpcionCaso o = opciones.get(d.getIdOpcion());
            String quien = usuarioRepository.findById(d.getIdUsuario()).map(u -> u.getNombre()).orElse(null);
            resultado.add(new DecisionEmpresaResponse(e.getId(), e.getCodigoEmpresa(), e.getNombre(), true,
                    d.getIdOpcion(), o != null ? o.getOpcion() : null, o != null ? o.getResultado() : null,
                    quien, d.getFechaDecision()));
        }
        return resultado;
    }

    private void validarSinDecisiones(Long idCaso) {
        if (decisionCasoRepository.existsByIdCaso(idCaso)) {
            throw new IllegalArgumentException(
                    "El caso ya tiene decisiones de empresas; no se puede editar ni eliminar");
        }
    }

    private void validarDatos(CasoRequest r, Simulacion simulacion) {
        if (r.getPenalizacionMin().compareTo(r.getPenalizacionMax()) > 0) {
            throw new IllegalArgumentException(
                    "La penalización mínima no puede ser mayor que la máxima");
        }
        if (r.getFechaVisualizacion().isAfter(r.getFechaInicioPartida())) {
            throw new IllegalArgumentException(
                    "La fecha de visualización no puede ser posterior al inicio de la partida");
        }
        if (!r.getFechaFinPartida().isAfter(r.getFechaInicioPartida())) {
            throw new IllegalArgumentException(
                    "La fecha de fin de la partida debe ser posterior a la de inicio");
        }
        if (r.getFechaInicioPartida().toLocalDate().isBefore(simulacion.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La partida no puede iniciar antes de la fecha de inicio de la simulación");
        }
        if (simulacion.getFechaFin() != null
                && r.getFechaFinPartida().toLocalDate().isAfter(simulacion.getFechaFin())) {
            throw new IllegalArgumentException(
                    "La partida no puede terminar después de la fecha de fin de la simulación");
        }
    }

    private void validarSimulacionEditable(Simulacion simulacion) {
        if (simulacion.getEstado() != EstadoSimulacion.BORRADOR
                && simulacion.getEstado() != EstadoSimulacion.PROGRAMADA) {
            throw new IllegalArgumentException(
                    "Solo se pueden modificar casos de simulaciones BORRADOR o PROGRAMADA");
        }
    }

    private void copiar(CasoRequest r, Caso c) {
        c.setNombreEmpresa(r.getNombre().trim());
        c.setTipo(r.getTipo());
        c.setMision(r.getMision());
        c.setVision(r.getVision());
        c.setActivoTotal(r.getFinanciero().getActivoTotal());
        c.setPasivoTotal(r.getFinanciero().getPasivoTotal());
        c.setPatrimonio(r.getFinanciero().getPatrimonio());
        c.setUtilidadNeta(r.getFinanciero().getUtilidadNeta());
        c.setPenalizacionMin(r.getPenalizacionMin());
        c.setPenalizacionMax(r.getPenalizacionMax());
        c.setFechaVisualizacion(r.getFechaVisualizacion());
        c.setFechaInicio(r.getFechaInicioPartida());
        c.setFechaFin(r.getFechaFinPartida());
        c.setAsignacionEquipos(r.getAsignacionEquipos() != null
                ? r.getAsignacionEquipos() : AsignacionEquipos.MANUAL);

        List<OpcionCaso> opciones = new ArrayList<>();
        int orden = 1;
        for (var o : r.getOpciones()) {
            opciones.add(new OpcionCaso(null, orden++, o.getOpcion().trim(), o.getResultado().trim()));
        }
        c.setOpciones(opciones);
    }

    private Contexto obtenerYValidar(Long idCaso, String correoDocente) {
        Caso caso = casoRepository.findById(idCaso)
                .orElseThrow(() -> new IllegalArgumentException("Caso no encontrado"));
        Simulacion simulacion = obtenerSimulacionYValidar(caso.getIdSimulacion(), correoDocente);
        return new Contexto(caso, simulacion);
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

    private CasoResponse toResponse(Caso c) {
        return new CasoResponse(
                c.getId(), c.getIdSimulacion(),
                c.getNombreEmpresa(), c.getTipo(), c.getEstado(), c.getMision(), c.getVision(),
                new FinancieroCaso(c.getActivoTotal(), c.getPasivoTotal(), c.getPatrimonio(), c.getUtilidadNeta()),
                c.getPenalizacionMin(), c.getPenalizacionMax(),
                c.getFechaVisualizacion(), c.getFechaInicio(), c.getFechaFin(),
                c.getAsignacionEquipos(),
                c.getOpciones().stream()
                        .map(o -> new OpcionCasoResponse(o.getId(), o.getOrden(), o.getOpcion(), o.getResultado()))
                        .toList()
        );
    }

    private record Contexto(Caso caso, Simulacion simulacion) {
    }
}

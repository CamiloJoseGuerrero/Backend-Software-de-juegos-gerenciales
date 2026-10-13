package com.estratego.application.usecase;

import com.estratego.application.dto.docente.*;
import com.estratego.domain.model.caso.*;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.*;
import com.estratego.domain.service.OrdenCasos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private final IntegranteRepository integranteRepository;

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

        List<Caso> existentes = casoRepository.findByIdSimulacion(idSimulacion);
        List<Caso> despues = new ArrayList<>(existentes);
        despues.add(caso);
        validarPosicion(caso, despues, OrdenCasos.primero(existentes), "mover");

        boolean primero = esPrimero(caso, despues);
        return toResponse(casoRepository.save(caso), primero);
    }

    /** Todos los casos de todas las simulaciones del docente (pestaña "Casos"). */
    public List<CasoResponse> listarDelDocente(String correoDocente) {
        Long idDocente = resolverDocenteId(correoDocente);
        List<Long> idsSimulacion = simulacionRepository.findByIdUsuarioCoordinador(idDocente).stream()
                .map(Simulacion::getId)
                .toList();
        return conPrimerCaso(casoRepository.findByIdSimulacionIn(idsSimulacion));
    }

    public List<CasoResponse> listarPorSimulacion(Long idSimulacion, String correoDocente) {
        obtenerSimulacionYValidar(idSimulacion, correoDocente);
        return conPrimerCaso(casoRepository.findByIdSimulacion(idSimulacion));
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
        List<Caso> existentes = casoRepository.findByIdSimulacion(caso.getIdSimulacion());
        Optional<Caso> primeroAntes = OrdenCasos.primero(existentes);
        copiar(request, caso);

        List<Caso> despues = new ArrayList<>(existentes.stream()
                .filter(c -> !Objects.equals(c.getId(), caso.getId())).toList());
        despues.add(caso);
        validarPosicion(caso, despues, primeroAntes, "mover");

        boolean primero = esPrimero(caso, despues);
        return toResponse(casoRepository.save(caso), primero);
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
        validarEmpresasConLider(caso.getIdSimulacion());

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
        List<Caso> existentes = casoRepository.findByIdSimulacion(ctx.caso().getIdSimulacion());
        List<Caso> despues = existentes.stream().filter(c -> !Objects.equals(c.getId(), id)).toList();
        validarPosicion(null, despues, OrdenCasos.primero(existentes), "eliminar");
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

    /**
     * Una empresa con integrantes y sin líder no podría decidir y acumularía penalización
     * sin que nadie lo note: no se deja activar el caso hasta que tenga líder.
     * Las empresas sin integrantes no bloquean.
     */
    private void validarEmpresasConLider(Long idSimulacion) {
        List<String> sinLider = new ArrayList<>();
        for (Empresa e : empresaRepository.findByIdSimulacion(idSimulacion)) {
            var integrantes = integranteRepository.findByIdEmpresa(e.getId());
            if (!integrantes.isEmpty() && integrantes.stream().noneMatch(i -> i.isEsLider())) {
                sinLider.add(e.getCodigoEmpresa() + " (" + e.getNombre() + ")");
            }
        }
        if (!sinLider.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se puede activar el caso: estas empresas no tienen líder: " + String.join(", ", sinLider));
        }
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

    /**
     * D2: solo el primer caso de la simulación define el balance inicial.
     * <ul>
     *   <li>Si el caso que se guarda queda primero: 19 partidas y balance que cuadre.</li>
     *   <li>Si por mover fechas o eliminar pasa a ser primero otro caso: ese debe tener balance completo
     *       que cuadre. Solo se revisa cuando el primero cambia, para no bloquear simulaciones viejas.</li>
     * </ul>
     *
     * @param guardado el caso que se crea o edita (null al eliminar)
     * @param despues  los casos de la simulación como quedarían
     * @param accion   "mover" o "eliminar", para el mensaje
     */
    private void validarPosicion(Caso guardado, List<Caso> despues, Optional<Caso> primeroAntes, String accion) {
        Optional<Caso> primero = OrdenCasos.primero(despues);
        if (primero.isEmpty()) return;
        Caso p = primero.get();
        if (p == guardado) {
            EstadoFinanciero ef = guardado.getFinanciero();
            List<String> faltan = ef.partidasFaltantes();
            if (!faltan.isEmpty()) {
                throw new CampoInvalidoException("financiero",
                        "El primer caso de la simulación debe traer las 19 partidas; faltan: "
                                + String.join(", ", faltan));
            }
            if (!ef.cuadra()) {
                throw new CampoInvalidoException("financiero", String.format(
                        "El balance no cuadra: activo total %s, pasivo + patrimonio %s",
                        ef.activoTotal().toPlainString(), ef.pasivoTotal().add(ef.patrimonio()).toPlainString()));
            }
            return;
        }
        boolean cambioElPrimero = primeroAntes.isEmpty()
                || !Objects.equals(primeroAntes.get().getId(), p.getId());
        if (cambioElPrimero && !balanceInicialValido(p)) {
            throw new CampoInvalidoException("financiero", String.format(
                    "Al %s este caso, «%s» pasaría a ser el primero y no tiene balance inicial",
                    accion, p.getNombreEmpresa()));
        }
    }

    private static boolean balanceInicialValido(Caso c) {
        EstadoFinanciero ef = c.getFinanciero();
        return ef != null && ef.partidasFaltantes().isEmpty() && ef.cuadra();
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
        // El backend es la fuente de verdad: los totales se calculan de las partidas
        EstadoFinanciero ef = r.getFinanciero().aEstadoFinanciero();
        c.setFinanciero(ef);
        // Casos que no son el primero pueden venir sin balance: sus totales quedan null
        boolean conBalance = ef.balanceCompleto();
        c.setActivoTotal(conBalance ? ef.activoTotal() : null);
        c.setPasivoTotal(conBalance ? ef.pasivoTotal() : null);
        c.setPatrimonio(conBalance ? ef.patrimonio() : null);
        c.setUtilidadNeta(ef.utilidadNeta());
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
            opciones.add(new OpcionCaso(null, orden++, o.getOpcion().trim(), o.getResultado().trim(),
                    o.getImpacto() != null ? o.getImpacto().aDominio() : null));
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

    /** esPrimerCaso por simulación (la lista puede traer casos de varias). */
    private List<CasoResponse> conPrimerCaso(List<Caso> casos) {
        Map<Long, Long> primeroPorSimulacion = casos.stream()
                .collect(Collectors.groupingBy(Caso::getIdSimulacion)).values().stream()
                .map(OrdenCasos::primero)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(Caso::getIdSimulacion, Caso::getId));
        return casos.stream()
                .map(c -> toResponse(c, Objects.equals(primeroPorSimulacion.get(c.getIdSimulacion()), c.getId())))
                .toList();
    }

    private boolean esPrimero(Caso caso, List<Caso> casosDeLaSimulacion) {
        return OrdenCasos.primero(casosDeLaSimulacion).map(p -> p == caso).orElse(false);
    }

    private CasoResponse toResponse(Caso c) {
        List<Caso> casos = casoRepository.findByIdSimulacion(c.getIdSimulacion());
        boolean primero = OrdenCasos.primero(casos)
                .map(p -> Objects.equals(p.getId(), c.getId()))
                .orElse(false);
        return toResponse(c, primero);
    }

    private CasoResponse toResponse(Caso c, boolean esPrimerCaso) {
        return new CasoResponse(
                c.getId(), c.getIdSimulacion(),
                c.getNombreEmpresa(), c.getTipo(), c.getEstado(), c.getMision(), c.getVision(),
                FinancieroCaso.de(c),
                c.getPenalizacionMin(), c.getPenalizacionMax(),
                c.getFechaVisualizacion(), c.getFechaInicio(), c.getFechaFin(),
                c.getAsignacionEquipos(),
                c.getOpciones().stream()
                        .map(o -> new OpcionCasoResponse(o.getId(), o.getOrden(), o.getOpcion(), o.getResultado(),
                                o.getImpacto()))
                        .toList(),
                esPrimerCaso
        );
    }

    private record Contexto(Caso caso, Simulacion simulacion) {
    }
}

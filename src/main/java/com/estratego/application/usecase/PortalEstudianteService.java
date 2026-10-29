package com.estratego.application.usecase;

import com.estratego.application.dto.docente.BoletinCasoResponse;
import com.estratego.application.dto.docente.EstadoFinancieroEmpresaResponse;
import com.estratego.application.dto.docente.FinancieroCaso;
import com.estratego.application.dto.empresa.HistoriaEmpresaResponse;
import com.estratego.application.dto.estudiante.*;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.DecisionCaso;
import com.estratego.domain.model.caso.EstadoCaso;
import com.estratego.domain.model.caso.OpcionCaso;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.model.integrante.Integrante;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.model.usuario.Usuario;
import com.estratego.domain.repository.*;
import com.estratego.domain.service.OrdenCasos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lo que ve y hace un estudiante. Reglas (🎨 propuesta del equipo salvo donde se indica):
 * - Solo ve simulaciones donde es integrante de alguna empresa.
 * - Un caso es visible desde su fechaVisualizacion y si la simulación no está en BORRADOR.
 * - Las opciones se muestran desde fechaInicioPartida y sin el Resultado.
 * - La decisión la toma el líder por toda la empresa, dentro de la partida, y es permanente.
 */
@Service
@RequiredArgsConstructor
public class PortalEstudianteService {

    private final UsuarioRepository usuarioRepository;
    private final IntegranteRepository integranteRepository;
    private final EmpresaRepository empresaRepository;
    private final SimulacionRepository simulacionRepository;
    private final CasoRepository casoRepository;
    private final DecisionCasoRepository decisionCasoRepository;
    private final EstadoFinancieroEmpresaRepository estadoFinancieroEmpresaRepository;
    private final BoletinCasoRepository boletinCasoRepository;

    public List<MiSimulacionResponse> misSimulaciones(String correo) {
        List<MiSimulacionResponse> resultado = new ArrayList<>();
        for (Participacion p : participaciones(resolverUsuarioId(correo))) {
            resultado.add(new MiSimulacionResponse(
                    p.simulacion().getId(), p.simulacion().getNombre(),
                    p.simulacion().getFechaInicio(), p.simulacion().getFechaFin(), p.simulacion().getEstado(),
                    p.empresa().getId(), p.empresa().getCodigoEmpresa(), p.empresa().getNombre(),
                    p.integrante().getDepartamento(), p.integrante().isEsLider()));
        }
        return resultado;
    }

    public MiEmpresaResponse miEmpresa(Long idEmpresa, String correo) {
        Long idUsuario = resolverUsuarioId(correo);
        integranteRepository.findByIdEmpresaAndIdUsuario(idEmpresa, idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No perteneces a esta empresa"));

        Empresa e = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        List<CompaneroResponse> integrantes = integranteRepository.findByIdEmpresa(idEmpresa).stream()
                .map(i -> {
                    Usuario u = usuarioRepository.findById(i.getIdUsuario()).orElse(null);
                    return new CompaneroResponse(i.getIdUsuario(),
                            u != null ? u.getNombre() : null,
                            u != null ? u.getCorreo() : null,
                            i.getDepartamento(), i.isEsLider());
                })
                .toList();

        return new MiEmpresaResponse(e.getId(), e.getIdSimulacion(), e.getCodigoEmpresa(), e.getNombre(),
                e.getEstrategia(), e.getTipoJugador(), e.getEstado(), integrantes);
    }

    /**
     * Historia financiera de la empresa del estudiante a lo largo de los casos jugados.
     */
    public List<HistoriaEmpresaResponse> historiaEmpresa(Long idEmpresa, String correo) {
        Long idUsuario = resolverUsuarioId(correo);
        integranteRepository.findByIdEmpresaAndIdUsuario(idEmpresa, idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No perteneces a esta empresa"));

        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        List<EstadoFinancieroEmpresa> estados = estadoFinancieroEmpresaRepository.listarPorEmpresa(idEmpresa);
        Map<Long, Map<MomentoEstado, EstadoFinancieroEmpresa>> porCaso = estados.stream()
                .collect(Collectors.groupingBy(
                        EstadoFinancieroEmpresa::getIdCaso,
                        Collectors.toMap(EstadoFinancieroEmpresa::getMomento, Function.identity(), (a, b) -> b)
                ));

        List<Caso> casosOrdenados = OrdenCasos.ordenados(casoRepository.findByIdSimulacion(empresa.getIdSimulacion()));
        List<HistoriaEmpresaResponse> resultado = new ArrayList<>();
        for (Caso caso : casosOrdenados) {
            Map<MomentoEstado, EstadoFinancieroEmpresa> momentos = porCaso.get(caso.getId());
            if (momentos == null || momentos.isEmpty()) {
                continue;
            }
            EstadoFinancieroEmpresa inicial = momentos.get(MomentoEstado.INICIAL);
            EstadoFinancieroEmpresa finalEstado = momentos.get(MomentoEstado.FINAL);

            resultado.add(new HistoriaEmpresaResponse(
                    caso.getId(),
                    caso.getNombreEmpresa(),
                    caso.getFechaInicio(),
                    EstadoFinancieroEmpresaResponse.de(inicial),
                    EstadoFinancieroEmpresaResponse.de(finalEstado)
            ));
        }
        return resultado;
    }

    public List<CasoEstudianteResponse> casosDeSimulacion(Long idSimulacion, String correo) {
        Simulacion simulacion = validarParticipacion(idSimulacion, resolverUsuarioId(correo));
        LocalDateTime ahora = LocalDateTime.now();

        List<Caso> todos = casoRepository.findByIdSimulacion(idSimulacion);
        return todos.stream()
                .filter(c -> esVisible(c, simulacion, ahora))
                .map(c -> toResponse(c, simulacion, todos, ahora))
                .toList();
    }

    public CasoEstudianteResponse caso(Long idCaso, String correo) {
        Long idUsuario = resolverUsuarioId(correo);
        Caso caso = casoRepository.findById(idCaso)
                .orElseThrow(() -> new IllegalArgumentException("Caso no encontrado"));
        Simulacion simulacion = validarParticipacion(caso.getIdSimulacion(), idUsuario);

        LocalDateTime ahora = LocalDateTime.now();
        if (!esVisible(caso, simulacion, ahora)) {
            throw new IllegalArgumentException("Este caso todavía no está disponible");
        }
        List<Caso> todos = casoRepository.findByIdSimulacion(caso.getIdSimulacion());
        return toResponse(caso, simulacion, todos, ahora);
    }

    /**
     * El caso ACTIVO y visible del estudiante. Vacío si no hay ninguno.
     * <p>
     * Con {@code idSimulacion} se limita a esa simulación. Sin él, si el estudiante está en
     * varias simulaciones con caso activo, elige por la fase del caso:
     * <ol>
     *   <li>partida en curso (si hay varias: la simulación más reciente),</li>
     *   <li>el próximo caso por empezar (el de inicio más cercano),</li>
     *   <li>el caso terminado más reciente.</li>
     * </ol>
     * Decidir no cambia la elección: depende solo de fechas, para que el estudiante
     * siga viendo el caso en el que acaba de decidir.
     */
    public Optional<CasoActualResponse> casoActual(String correo, Long idSimulacion) {
        LocalDateTime ahora = LocalDateTime.now();

        List<CasoActualResponse> candidatos = new ArrayList<>();
        for (Participacion p : participaciones(resolverUsuarioId(correo))) {
            if (idSimulacion != null && !idSimulacion.equals(p.simulacion().getId())) continue;

            Optional<Caso> activo = casoRepository.findActivoBySimulacion(p.simulacion().getId())
                    .filter(c -> esVisible(c, p.simulacion(), ahora));
            if (activo.isEmpty()) continue;

            Caso caso = activo.get();
            List<Caso> todos = casoRepository.findByIdSimulacion(p.simulacion().getId());

            DecisionResponse decision = decisionCasoRepository
                    .findByIdCasoAndIdEmpresa(caso.getId(), p.empresa().getId())
                    .map(d -> toDecisionResponse(d, caso))
                    .orElse(null);

            boolean puedeDecidir = p.integrante().isEsLider()
                    && decision == null
                    && enPartida(caso, p.simulacion(), ahora, todos);

            EstadoFinancieroEmpresa inicial = estadoFinancieroEmpresaRepository
                    .buscar(p.empresa().getId(), caso.getId(), MomentoEstado.INICIAL)
                    .orElse(null);
            EstadoFinancieroEmpresa finalEstado = estadoFinancieroEmpresaRepository
                    .buscar(p.empresa().getId(), caso.getId(), MomentoEstado.FINAL)
                    .orElse(null);

            FinancieroEmpresaCasoResponse financieroEmpresa =
                    new FinancieroEmpresaCasoResponse(
                            EstadoFinancieroEmpresaResponse.de(inicial),
                            EstadoFinancieroEmpresaResponse.de(finalEstado)
                    );

            BoletinCasoResponse boletin = boletinCasoRepository.findByIdCaso(caso.getId())
                    .filter(b -> caso.getFechaVisualizacion() == null || !ahora.isBefore(caso.getFechaVisualizacion()))
                    .map(b -> new BoletinCasoResponse(
                            b.getIdCaso(),
                            b.getInflacion(),
                            b.getTasaInteres(),
                            b.getTasaCambio(),
                            b.getNota(),
                            b.getPublicadoEn(),
                            b.getActualizadoEn()
                    ))
                    .orElse(null);

            candidatos.add(new CasoActualResponse(
                    p.simulacion().getId(), p.simulacion().getNombre(),
                    p.empresa().getId(), p.empresa().getNombre(),
                    p.integrante().isEsLider(), puedeDecidir,
                    toResponse(caso, p.simulacion(), todos, ahora),
                    decision,
                    financieroEmpresa,
                    boletin));
        }

        // min() conserva el primero en empate: las participaciones vienen de la simulación más reciente
        return candidatos.stream().min(prioridadCasoActual());
    }

    /** Orden de {@link #casoActual}: menor = más relevante para el estudiante. */
    private static Comparator<CasoActualResponse> prioridadCasoActual() {
        Comparator<CasoActualResponse> porFase = Comparator.comparingInt(PortalEstudianteService::fase);
        // Dentro de la fase: en curso -> empate (gana la simulación más reciente);
        // próximos -> el que empieza antes; terminados -> el que terminó más tarde
        return porFase.thenComparing((a, b) -> switch (fase(a)) {
            case 1 -> a.getCaso().getFechaInicioPartida().compareTo(b.getCaso().getFechaInicioPartida());
            case 2 -> b.getCaso().getFechaFinPartida().compareTo(a.getCaso().getFechaFinPartida());
            default -> 0;
        });
    }

    /** 0 = partida en curso, 1 = próxima, 2 = terminada. */
    private static int fase(CasoActualResponse r) {
        CasoEstudianteResponse c = r.getCaso();
        if (!c.isPartidaIniciada()) return 1;
        if (!c.isPartidaFinalizada()) return 0;
        return 2;
    }

    /** El líder elige la opción de su empresa. Permanente: no se puede cambiar después. */
    @Transactional
    public DecisionResponse decidir(DecisionRequest request, String correo) {
        Long idUsuario = resolverUsuarioId(correo);
        Caso caso = casoRepository.findById(request.getIdCaso())
                .orElseThrow(() -> new IllegalArgumentException("Caso no encontrado"));

        Participacion p = participaciones(idUsuario).stream()
                .filter(x -> x.simulacion().getId().equals(caso.getIdSimulacion()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No participas en esta simulación"));

        if (!p.integrante().isEsLider()) {
            throw new IllegalArgumentException("Solo el líder de la empresa puede tomar la decisión");
        }
        if (caso.getEstado() != EstadoCaso.ACTIVO) {
            throw new IllegalArgumentException("Este caso no está activo");
        }

        LocalDateTime ahora = LocalDateTime.now();
        List<Caso> todos = casoRepository.findByIdSimulacion(caso.getIdSimulacion());
        if (ahora.isBefore(caso.getFechaInicio())) {
            throw new IllegalArgumentException("La partida aún no ha iniciado");
        }
        if (ahora.isAfter(caso.getFechaFin()) || esPartidaFinalizada(caso, p.simulacion(), todos, ahora)) {
            throw new IllegalArgumentException("La partida ya terminó");
        }
        if (!enPartida(caso, p.simulacion(), ahora, todos)) {
            throw new IllegalArgumentException("La simulación no está en curso");
        }

        if (decisionCasoRepository.findByIdCasoAndIdEmpresa(caso.getId(), p.empresa().getId()).isPresent()) {
            throw new IllegalArgumentException("Tu empresa ya tomó su decisión y no se puede cambiar");
        }

        boolean opcionValida = caso.getOpciones().stream()
                .anyMatch(o -> o.getId().equals(request.getIdOpcion()));
        if (!opcionValida) {
            throw new IllegalArgumentException("La opción no pertenece a este caso");
        }

        DecisionCaso guardada = decisionCasoRepository.save(new DecisionCaso(
                null, caso.getId(), p.empresa().getId(), request.getIdOpcion(), idUsuario, ahora));
        return toDecisionResponse(guardada, caso);
    }

    // --- helpers ---

    private boolean enPartida(Caso c, Simulacion s, LocalDateTime ahora, List<Caso> todos) {
        boolean simulacionAbierta = s.getEstado() == EstadoSimulacion.PROGRAMADA
                || s.getEstado() == EstadoSimulacion.EN_CURSO;
        return simulacionAbierta
                && c.getEstado() == EstadoCaso.ACTIVO
                && !ahora.isBefore(c.getFechaInicio())
                && !esPartidaFinalizada(c, s, todos, ahora);
    }

    private boolean enPartida(Caso c, Simulacion s, LocalDateTime ahora) {
        return enPartida(c, s, ahora, null);
    }

    private boolean esPartidaFinalizada(Caso c, Simulacion simulacion, List<Caso> casosSimulacion, LocalDateTime ahora) {
        if (simulacion != null && simulacion.getEstado() == EstadoSimulacion.FINALIZADA) {
            return true;
        }
        if (ahora.isAfter(c.getFechaFin())) {
            return true;
        }
        if (casosSimulacion != null && !casosSimulacion.isEmpty()) {
            List<Caso> ordenados = OrdenCasos.ordenados(casosSimulacion);
            int pos = -1;
            for (int i = 0; i < ordenados.size(); i++) {
                if (Objects.equals(ordenados.get(i).getId(), c.getId())) {
                    pos = i;
                    break;
                }
            }
            if (pos != -1) {
                for (int j = pos + 1; j < ordenados.size(); j++) {
                    Caso posterior = ordenados.get(j);
                    if (posterior.getActivadoEn() != null || posterior.getEstado() == EstadoCaso.ACTIVO) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean esVisible(Caso c, Simulacion s, LocalDateTime ahora) {
        return s.getEstado() != EstadoSimulacion.BORRADOR
                && !ahora.isBefore(c.getFechaVisualizacion());
    }

    /** Simulaciones en las que participa el usuario, la más reciente primero. */
    private List<Participacion> participaciones(Long idUsuario) {
        List<Participacion> lista = new ArrayList<>();
        for (Integrante integrante : integranteRepository.findByIdUsuario(idUsuario)) {
            Optional<Empresa> empresa = empresaRepository.findById(integrante.getIdEmpresa());
            if (empresa.isEmpty()) continue;
            Optional<Simulacion> simulacion = simulacionRepository.findById(empresa.get().getIdSimulacion());
            if (simulacion.isEmpty()) continue;
            lista.add(new Participacion(integrante, empresa.get(), simulacion.get()));
        }
        lista.sort(Comparator.comparing((Participacion p) -> p.simulacion().getFechaInicio(),
                Comparator.nullsLast(Comparator.reverseOrder())));
        return lista;
    }

    private Simulacion validarParticipacion(Long idSimulacion, Long idUsuario) {
        Simulacion simulacion = simulacionRepository.findById(idSimulacion)
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));

        if (!integranteRepository.existsEnSimulacion(idSimulacion, idUsuario)) {
            throw new IllegalArgumentException("No participas en esta simulación");
        }
        return simulacion;
    }

    private Long resolverUsuarioId(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"))
                .getId();
    }

    private DecisionResponse toDecisionResponse(DecisionCaso d, Caso caso) {
        OpcionCaso o = caso.getOpciones().stream()
                .filter(x -> x.getId().equals(d.getIdOpcion()))
                .findFirst().orElse(null);
        String quien = usuarioRepository.findById(d.getIdUsuario()).map(Usuario::getNombre).orElse(null);
        return new DecisionResponse(d.getIdCaso(), d.getIdEmpresa(), d.getIdOpcion(),
                o != null ? o.getOpcion() : null, o != null ? o.getResultado() : null,
                o != null ? o.getImpacto() : null,
                quien, d.getFechaDecision());
    }

    private CasoEstudianteResponse toResponse(Caso c, Simulacion simulacion, List<Caso> casosSimulacion, LocalDateTime ahora) {
        boolean iniciada = !ahora.isBefore(c.getFechaInicio());
        boolean finalizada = esPartidaFinalizada(c, simulacion, casosSimulacion, ahora);
        List<OpcionEstudianteResponse> opciones = iniciada
                ? c.getOpciones().stream()
                    .map(o -> new OpcionEstudianteResponse(o.getId(), o.getOrden(), o.getOpcion()))
                    .toList()
                : List.of();

        return new CasoEstudianteResponse(
                c.getId(), c.getIdSimulacion(),
                c.getNombreEmpresa(), c.getTipo(), c.getMision(), c.getVision(),
                FinancieroCaso.de(c),
                c.getPenalizacionMin(), c.getPenalizacionMax(),
                c.getFechaVisualizacion(), c.getFechaInicio(), c.getFechaFin(),
                iniciada, finalizada, opciones);
    }

    private CasoEstudianteResponse toResponse(Caso c, LocalDateTime ahora) {
        return toResponse(c, null, null, ahora);
    }

    private record Participacion(Integrante integrante, Empresa empresa, Simulacion simulacion) {
    }
}

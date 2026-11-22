package com.estratego.application.usecase;

import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.DecisionCaso;
import com.estratego.domain.model.caso.EstadoCaso;
import com.estratego.domain.model.caso.OpcionCaso;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.model.financiero.OrigenEstado;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.*;
import com.estratego.domain.service.GeneradorAleatorio;
import com.estratego.domain.service.MotorArrastre;
import com.estratego.domain.service.OrdenCasos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculoEstadosService {

    private final SimulacionRepository simulacionRepository;
    private final CasoRepository casoRepository;
    private final EmpresaRepository empresaRepository;
    private final DecisionCasoRepository decisionCasoRepository;
    private final EstadoFinancieroEmpresaRepository estadoRepository;
    private final Clock clock;
    private final GeneradorAleatorio generadorAleatorio;

    public record Resultado(int iniciales, int finales) {
        public static final Resultado NADA = new Resultado(0, 0);

        public boolean hizoAlgo() {
            return iniciales > 0 || finales > 0;
        }
    }

    public List<Long> simulacionesEnCurso() {
        return simulacionRepository.findByEstado(EstadoSimulacion.EN_CURSO).stream()
                .map(Simulacion::getId)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Resultado procesar(Long idSimulacion) {
        Simulacion simulacion = simulacionRepository.findById(idSimulacion).orElse(null);
        if (simulacion == null || (simulacion.getEstado() != EstadoSimulacion.EN_CURSO
                && simulacion.getEstado() != EstadoSimulacion.FINALIZADA)) {
            return Resultado.NADA;
        }
        boolean finalizada = simulacion.getEstado() == EstadoSimulacion.FINALIZADA;
        LocalDateTime ahora = LocalDateTime.now(clock);

        List<Caso> todos = casoRepository.findByIdSimulacion(idSimulacion);
        Optional<Caso> primero = OrdenCasos.primero(todos);
        if (primero.isEmpty()) return Resultado.NADA;
        if (!balanceCompleto(primero.get())) {
            log.warn("Simulación {}: el primer caso no tiene balance completo; no se calculan estados", idSimulacion);
            return Resultado.NADA;
        }

        List<Caso> jugados = OrdenCasos.ordenados(todos.stream()
                .filter(c -> c.getActivadoEn() != null || c.getEstado() == EstadoCaso.ACTIVO)
                .toList());
        if (jugados.isEmpty()) return Resultado.NADA;
        if (jugados.stream().anyMatch(c -> c.getFinanciero() == null)) {
            log.warn("Simulación {}: hay casos sin estado financiero (anteriores al modelo de 19 partidas); "
                    + "no se calculan estados", idSimulacion);
            return Resultado.NADA;
        }

        Map<Long, Map<Long, Map<MomentoEstado, EstadoFinancieroEmpresa>>> existentes = new HashMap<>();
        for (Caso c : jugados) {
            for (EstadoFinancieroEmpresa e : estadoRepository.listarPorCaso(c.getId())) {
                existentes.computeIfAbsent(e.getIdEmpresa(), k -> new HashMap<>())
                        .computeIfAbsent(e.getIdCaso(), k -> new EnumMap<>(MomentoEstado.class))
                        .put(e.getMomento(), e);
            }
        }
        Set<Long> casosSembrados = existentes.values().stream()
                .flatMap(m -> m.keySet().stream())
                .collect(Collectors.toSet());
        Map<Long, Map<Long, DecisionCaso>> decisiones = new HashMap<>();

        int iniciales = 0;
        int finales = 0;
        for (Empresa empresa : empresaRepository.findByIdSimulacion(idSimulacion)) {
            Map<Long, Map<MomentoEstado, EstadoFinancieroEmpresa>> propios =
                    existentes.getOrDefault(empresa.getId(), Map.of());
            EstadoFinanciero finalAnterior = null;
            boolean participa = false;

            for (int i = 0; i < jugados.size(); i++) {
                Caso caso = jugados.get(i);
                boolean cerrado = finalizada || ahora.isAfter(caso.getFechaFin()) || i < jugados.size() - 1;
                Map<MomentoEstado, EstadoFinancieroEmpresa> delCaso = propios.getOrDefault(caso.getId(), Map.of());

                EstadoFinancieroEmpresa inicial = delCaso.get(MomentoEstado.INICIAL);
                if (inicial == null) {
                    if (cerrado && !participa && casosSembrados.contains(caso.getId())
                            && !decisiones.computeIfAbsent(caso.getId(), this::decisionesPorEmpresa)
                                    .containsKey(empresa.getId())) {
                        continue;
                    }
                    boolean hereda = finalAnterior != null;
                    inicial = estadoRepository.guardar(new EstadoFinancieroEmpresa(null, empresa.getId(),
                            caso.getId(), MomentoEstado.INICIAL,
                            hereda ? OrigenEstado.HEREDADO : OrigenEstado.DOCENTE, null,
                            hereda ? MotorArrastre.inicialHeredado(finalAnterior, caso)
                                    : MotorArrastre.inicialDocente(primero.get(), caso),
                            ahora));
                    iniciales++;
                }
                participa = true;

                if (!cerrado) break;

                EstadoFinancieroEmpresa fin = delCaso.get(MomentoEstado.FINAL);
                if (fin == null) {
                    DecisionCaso d = decisiones
                            .computeIfAbsent(caso.getId(), this::decisionesPorEmpresa)
                            .get(empresa.getId());
                    fin = estadoRepository.guardar(d != null
                            ? conDecision(empresa, caso, inicial, d, ahora)
                            : penalizado(empresa, caso, inicial, ahora));
                    finales++;
                }
                finalAnterior = fin.getEstado();
            }
        }

        Resultado r = new Resultado(iniciales, finales);
        if (r.hizoAlgo()) {
            log.info("Simulación {}: {} estados iniciales y {} finales calculados", idSimulacion, iniciales, finales);
        }
        return r;
    }

    private EstadoFinancieroEmpresa conDecision(Empresa empresa, Caso caso, EstadoFinancieroEmpresa inicial,
                                                DecisionCaso d, LocalDateTime ahora) {
        OpcionCaso opcion = caso.getOpciones().stream()
                .filter(o -> Objects.equals(o.getId(), d.getIdOpcion()))
                .findFirst().orElse(null);
        return new EstadoFinancieroEmpresa(null, empresa.getId(), caso.getId(), MomentoEstado.FINAL,
                OrigenEstado.DECISION, d.getIdOpcion(),
                MotorArrastre.finalConDecision(inicial.getEstado(), opcion != null ? opcion.getImpacto() : null),
                ahora);
    }

    private EstadoFinancieroEmpresa penalizado(Empresa empresa, Caso caso, EstadoFinancieroEmpresa inicial,
                                               LocalDateTime ahora) {
        java.math.BigDecimal porcentaje = generadorAleatorio.entre(caso.getPenalizacionMin(), caso.getPenalizacionMax());
        return new EstadoFinancieroEmpresa(null, empresa.getId(), caso.getId(), MomentoEstado.FINAL,
                OrigenEstado.PENALIZACION, null,
                MotorArrastre.finalPenalizado(inicial.getEstado(), porcentaje), ahora, porcentaje);
    }

    private Map<Long, DecisionCaso> decisionesPorEmpresa(Long idCaso) {
        return decisionCasoRepository.findByIdCaso(idCaso).stream()
                .collect(Collectors.toMap(DecisionCaso::getIdEmpresa, Function.identity(), (a, b) -> a));
    }

    private static boolean balanceCompleto(Caso c) {
        EstadoFinanciero ef = c.getFinanciero();
        return ef != null && ef.partidasFaltantes().isEmpty() && ef.cuadra();
    }
}

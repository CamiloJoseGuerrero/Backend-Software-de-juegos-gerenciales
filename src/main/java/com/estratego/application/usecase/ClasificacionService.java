package com.estratego.application.usecase;

import com.estratego.application.dto.clasificacion.ClasificacionResponse;
import com.estratego.application.dto.clasificacion.FilaClasificacionResponse;
import com.estratego.application.dto.clasificacion.ResultadoCasoResponse;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.DecisionCaso;
import com.estratego.domain.model.caso.OpcionCaso;
import com.estratego.domain.model.caso.EstadoCaso;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.financiero.EstadoFinancieroEmpresa;
import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.model.financiero.OrigenEstado;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.*;
import com.estratego.domain.service.CalculadoraResultadoCaso;
import com.estratego.domain.service.OrdenCasos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Clasificación de una simulación: utilidad neta acumulada por empresa.
 *
 * <p>Encadenada (D2): la utilidad de cada caso sale del estado FINAL guardado de la empresa
 * (CalculoEstadosService), que ya incluye la decisión o la penalización sobre su balance heredado.
 * Una empresa sin estados en un caso que sí tiene estados calculados no participó en él (entró tarde)
 * y ese caso no le suma ni le resta.</p>
 *
 * <p>Respaldo: si un caso cerrado todavía no tiene ningún estado FINAL (simulaciones anteriores a D2,
 * o el cierre automático aún no pasó), se calcula al vuelo como antes.</p>
 *
 * <p>Un caso cuenta si se activó alguna vez y está cerrado: la simulación finalizó, ya pasó su fecha de fin
 * o ya se activó un caso posterior (misma regla que el cálculo de estados).</p>
 */
@Service
@RequiredArgsConstructor
public class ClasificacionService {

    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final IntegranteRepository integranteRepository;
    private final EmpresaRepository empresaRepository;
    private final CasoRepository casoRepository;
    private final DecisionCasoRepository decisionCasoRepository;
    private final EstadoFinancieroEmpresaRepository estadoRepository;

    /** El docente la ve en cualquier momento (vista previa mientras la simulación no termina). */
    public ClasificacionResponse paraDocente(Long idSimulacion, String correoDocente) {
        Simulacion simulacion = buscarSimulacion(idSimulacion);
        if (!resolverUsuarioId(correoDocente).equals(simulacion.getIdUsuarioCoordinador())) {
            throw new IllegalArgumentException("La simulación no pertenece a este docente");
        }
        return calcular(simulacion, LocalDateTime.now());
    }

    /** El estudiante solo la ve cuando la simulación está FINALIZADA. */
    public ClasificacionResponse paraEstudiante(Long idSimulacion, String correoEstudiante) {
        Simulacion simulacion = buscarSimulacion(idSimulacion);
        if (!integranteRepository.existsEnSimulacion(idSimulacion, resolverUsuarioId(correoEstudiante))) {
            throw new IllegalArgumentException("No participas en esta simulación");
        }
        if (simulacion.getEstado() != EstadoSimulacion.FINALIZADA) {
            throw new IllegalArgumentException("La clasificación estará disponible cuando la simulación finalice");
        }
        return calcular(simulacion, LocalDateTime.now());
    }

    ClasificacionResponse calcular(Simulacion simulacion, LocalDateTime ahora) {
        boolean finalizada = simulacion.getEstado() == EstadoSimulacion.FINALIZADA;

        List<Caso> jugados = OrdenCasos.ordenados(casoRepository.findByIdSimulacion(simulacion.getId()).stream()
                .filter(c -> c.getActivadoEn() != null || c.getEstado() == EstadoCaso.ACTIVO)
                .toList());
        List<Caso> casos = new ArrayList<>();
        for (int i = 0; i < jugados.size(); i++) {
            Caso c = jugados.get(i);
            if (finalizada || ahora.isAfter(c.getFechaFin()) || i < jugados.size() - 1) casos.add(c);
        }

        if (casos.isEmpty()) {
            return new ClasificacionResponse(simulacion.getId(), simulacion.getNombre(), simulacion.getEstado(),
                    finalizada, 0, List.of());
        }

        // Estados FINAL guardados por caso y empresa (vacío = el caso se calcula al vuelo)
        Map<Long, Map<Long, EstadoFinancieroEmpresa>> finalesPorCaso = new HashMap<>();
        for (Caso c : casos) {
            finalesPorCaso.put(c.getId(), estadoRepository.listarPorCaso(c.getId()).stream()
                    .filter(e -> e.getMomento() == MomentoEstado.FINAL)
                    .collect(Collectors.toMap(EstadoFinancieroEmpresa::getIdEmpresa, Function.identity(), (a, b) -> a)));
        }

        Map<Long, Map<Long, DecisionCaso>> decisionesPorCaso = new HashMap<>();
        for (Caso c : casos) {
            decisionesPorCaso.put(c.getId(), decisionCasoRepository.findByIdCaso(c.getId()).stream()
                    .collect(Collectors.toMap(DecisionCaso::getIdEmpresa, Function.identity(), (a, b) -> a)));
        }

        List<FilaClasificacionResponse> filas = new ArrayList<>();
        for (Empresa e : empresaRepository.findByIdSimulacion(simulacion.getId())) {
            BigDecimal acumulada = BigDecimal.ZERO;
            int sinDecision = 0;
            List<ResultadoCasoResponse> desglose = new ArrayList<>();

            for (Caso c : casos) {
                Map<Long, EstadoFinancieroEmpresa> finales = finalesPorCaso.get(c.getId());
                ResultadoCasoResponse r;
                if (!finales.isEmpty()) {
                    EstadoFinancieroEmpresa fin = finales.get(e.getId());
                    if (fin == null) continue; // la empresa no participó en este caso
                    r = desdeEstado(c, fin);
                } else {
                    DecisionCaso d = decisionesPorCaso.get(c.getId()).get(e.getId());
                    r = d != null ? conDecision(c, d) : sinDecision(c);
                }
                if (!r.isDecidio()) sinDecision++;
                acumulada = acumulada.add(r.getUtilidadDelCaso());
                desglose.add(r);
            }
            filas.add(new FilaClasificacionResponse(0, e.getId(), e.getCodigoEmpresa(), e.getNombre(),
                    acumulada, sinDecision, desglose));
        }

        filas.sort(Comparator.comparing(FilaClasificacionResponse::getUtilidadAcumulada).reversed()
                .thenComparing(FilaClasificacionResponse::getCodigoEmpresa,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        asignarPosiciones(filas);

        return new ClasificacionResponse(simulacion.getId(), simulacion.getNombre(), simulacion.getEstado(),
                finalizada, casos.size(), filas);
    }

    /** Resultado del caso tomado del estado FINAL guardado de la empresa. */
    private ResultadoCasoResponse desdeEstado(Caso c, EstadoFinancieroEmpresa fin) {
        boolean decidio = fin.getOrigen() == OrigenEstado.DECISION;
        String opcion = decidio ? c.getOpciones().stream()
                .filter(o -> Objects.equals(o.getId(), fin.getIdOpcion()))
                .map(OpcionCaso::getOpcion)
                .findFirst().orElse(null) : null;
        return new ResultadoCasoResponse(c.getId(), c.getNombreEmpresa(), c.getFechaFin(), decidio,
                decidio ? fin.getIdOpcion() : null, opcion,
                CalculadoraResultadoCaso.utilidadBase(c),
                decidio ? null : CalculadoraResultadoCaso.porcentajePenalizacion(c),
                fin.getEstado().utilidadNeta());
    }

    private ResultadoCasoResponse conDecision(Caso c, DecisionCaso d) {
        OpcionCaso opcion = c.getOpciones().stream()
                .filter(o -> o.getId().equals(d.getIdOpcion()))
                .findFirst().orElse(null);
        BigDecimal utilidad = CalculadoraResultadoCaso.utilidadConDecision(c, opcion != null ? opcion.getImpacto() : null);
        return new ResultadoCasoResponse(c.getId(), c.getNombreEmpresa(), c.getFechaFin(), true,
                d.getIdOpcion(), opcion != null ? opcion.getOpcion() : null,
                CalculadoraResultadoCaso.utilidadBase(c), null, utilidad);
    }

    private ResultadoCasoResponse sinDecision(Caso c) {
        return new ResultadoCasoResponse(c.getId(), c.getNombreEmpresa(), c.getFechaFin(), false,
                null, null, CalculadoraResultadoCaso.utilidadBase(c),
                CalculadoraResultadoCaso.porcentajePenalizacion(c), CalculadoraResultadoCaso.utilidadPenalizada(c));
    }

    /** Empate = misma posición; la siguiente salta (1, 1, 3). */
    private static void asignarPosiciones(List<FilaClasificacionResponse> filas) {
        for (int i = 0; i < filas.size(); i++) {
            boolean empataConAnterior = i > 0
                    && filas.get(i).getUtilidadAcumulada().compareTo(filas.get(i - 1).getUtilidadAcumulada()) == 0;
            filas.get(i).setPosicion(empataConAnterior ? filas.get(i - 1).getPosicion() : i + 1);
        }
    }

    private Simulacion buscarSimulacion(Long id) {
        return simulacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Simulación no encontrada"));
    }

    private Long resolverUsuarioId(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"))
                .getId();
    }
}

package com.estratego.application.usecase;

import com.estratego.application.dto.clasificacion.ClasificacionResponse;
import com.estratego.application.dto.clasificacion.FilaClasificacionResponse;
import com.estratego.application.dto.clasificacion.ResultadoCasoResponse;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.caso.DecisionCaso;
import com.estratego.domain.model.caso.OpcionCaso;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.*;
import com.estratego.domain.service.CalculadoraResultadoCaso;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Clasificación de una simulación: utilidad neta acumulada por empresa.
 * Se calcula al vuelo; un caso con decisiones ya no se puede editar, así que el resultado es estable.
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

        List<Caso> casos = casoRepository.findByIdSimulacion(simulacion.getId()).stream()
                .filter(c -> c.getActivadoEn() != null)
                .filter(c -> finalizada || ahora.isAfter(c.getFechaFin()))
                .sorted(Comparator.comparing(Caso::getFechaInicio))
                .toList();

        if (casos.isEmpty()) {
            return new ClasificacionResponse(simulacion.getId(), simulacion.getNombre(), simulacion.getEstado(),
                    finalizada, 0, List.of());
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
                DecisionCaso d = decisionesPorCaso.get(c.getId()).get(e.getId());
                ResultadoCasoResponse r = d != null ? conDecision(c, d) : sinDecision(c);
                if (d == null) sinDecision++;
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

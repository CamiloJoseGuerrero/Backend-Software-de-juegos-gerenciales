package com.estratego.application.usecase;

import com.estratego.application.dto.docente.ActualizarSimulacionRequest;
import com.estratego.application.dto.docente.CrearSimulacionRequest;
import com.estratego.application.dto.docente.SimulacionResponse;
import com.estratego.domain.model.caso.Caso;
import com.estratego.domain.model.empresa.Empresa;
import com.estratego.domain.model.financiero.EstadoFinanciero;
import com.estratego.domain.model.integrante.Integrante;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import com.estratego.domain.model.simulacion.Simulacion;
import com.estratego.domain.repository.CasoRepository;
import com.estratego.domain.repository.DecisionCasoRepository;
import com.estratego.domain.repository.EmpresaRepository;
import com.estratego.domain.repository.IntegranteRepository;
import com.estratego.domain.repository.SimulacionRepository;
import com.estratego.domain.service.OrdenCasos;
import com.estratego.domain.repository.UsuarioRepository;
import com.estratego.application.event.EstadosFinancierosPendientesEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulacionService {

    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CasoRepository casoRepository;
    private final EmpresaRepository empresaRepository;
    private final IntegranteRepository integranteRepository;
    private final DecisionCasoRepository decisionCasoRepository;

    /** Último aviso de "no arranca" por simulación, para no repetirlo en el log cada minuto. */
    private final java.util.Map<Long, List<ValidacionException.Problema>> avisosNoArranca =
            new java.util.concurrent.ConcurrentHashMap<>();

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
        List<ValidacionException.Problema> problemas = problemasParaArrancar(simulacion);
        if (!problemas.isEmpty()) {
            throw new ValidacionException("La simulación no está lista para programarse", problemas);
        }

        simulacion.setEstado(EstadoSimulacion.PROGRAMADA);
        return toResponse(simulacionRepository.save(simulacion));
    }

    @Transactional
    public SimulacionResponse iniciar(Long id, String correoCoordinador) {
        Simulacion simulacion = obtenerYValidar(id, correoCoordinador);

        // Idempotente: el cierre automático pudo haberla iniciado ya al llegar su fecha de inicio
        if (simulacion.getEstado() == EstadoSimulacion.EN_CURSO) {
            return toResponse(simulacion);
        }
        if (simulacion.getEstado() != EstadoSimulacion.PROGRAMADA) {
            throw new IllegalArgumentException("Solo se puede iniciar una simulación en PROGRAMADA");
        }

        if (simulacion.getFechaInicio().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Aún no ha llegado la fecha de inicio");
        }

        simulacion.setEstado(EstadoSimulacion.EN_CURSO);
        Simulacion guardada = simulacionRepository.save(simulacion);
        eventPublisher.publishEvent(new EstadosFinancierosPendientesEvent(id));
        return toResponse(guardada);
    }

    @Transactional
    public SimulacionResponse finalizar(Long id, String correoCoordinador) {
        Simulacion simulacion = obtenerYValidar(id, correoCoordinador);

        if (simulacion.getEstado() != EstadoSimulacion.EN_CURSO) {
            throw new IllegalArgumentException("Solo se puede finalizar una simulación EN_CURSO");
        }

        simulacion.setEstado(EstadoSimulacion.FINALIZADA);
        Simulacion guardada = simulacionRepository.save(simulacion);
        // D2: al finalizar se cierran todos los casos jugados
        eventPublisher.publishEvent(new EstadosFinancierosPendientesEvent(id));
        return toResponse(guardada);
    }

    /**
     * Ciclo de vida automático (lo llama el cierre automático cada minuto):
     * <ul>
     *   <li>PROGRAMADA → EN_CURSO cuando llega su fecha de inicio.</li>
     *   <li>EN_CURSO → FINALIZADA cuando termina el día de su fecha de fin (sin fecha de fin, solo a mano).</li>
     * </ul>
     * Los endpoints /iniciar y /finalizar siguen sirviendo para hacerlo antes.
     *
     * @return las simulaciones que cambiaron de estado (para calcular sus estados financieros)
     */
    @Transactional
    public List<Long> avanzarAutomaticamente(LocalDate hoy) {
        List<Long> cambiadas = new ArrayList<>();
        for (Simulacion s : simulacionRepository.findByEstado(EstadoSimulacion.PROGRAMADA)) {
            if (s.getFechaInicio() != null && !s.getFechaInicio().isAfter(hoy)) {
                // Si después de programarla quitaron casos o empresas, no arranca hasta que se corrija
                List<ValidacionException.Problema> problemas = problemasParaArrancar(s);
                if (!problemas.isEmpty()) {
                    // Solo se avisa la primera vez (o si cambian los problemas)
                    if (!problemas.equals(avisosNoArranca.put(s.getId(), problemas))) {
                        log.warn("Simulación {} no arranca: {}", s.getId(), problemas);
                    }
                    continue;
                }
                avisosNoArranca.remove(s.getId());
                s.setEstado(EstadoSimulacion.EN_CURSO);
                simulacionRepository.save(s);
                cambiadas.add(s.getId());
            }
        }
        for (Simulacion s : simulacionRepository.findByEstado(EstadoSimulacion.EN_CURSO)) {
            if (s.getFechaFin() != null && hoy.isAfter(s.getFechaFin())) {
                s.setEstado(EstadoSimulacion.FINALIZADA);
                simulacionRepository.save(s);
                if (!cambiadas.contains(s.getId())) cambiadas.add(s.getId());
            }
        }
        return cambiadas;
    }

    /** PROGRAMADA → BORRADOR (deja de arrancar sola). No se puede si ya hay decisiones. */
    @Transactional
    public SimulacionResponse desprogramar(Long id, String correoCoordinador) {
        Simulacion simulacion = obtenerYValidar(id, correoCoordinador);

        if (simulacion.getEstado() != EstadoSimulacion.PROGRAMADA) {
            throw new IllegalArgumentException("Solo se puede desprogramar una simulación PROGRAMADA");
        }
        boolean hayDecisiones = casoRepository.findByIdSimulacion(id).stream()
                .anyMatch(c -> decisionCasoRepository.existsByIdCaso(c.getId()));
        if (hayDecisiones) {
            throw new IllegalArgumentException("La simulación ya tiene decisiones de empresas; no se puede desprogramar");
        }

        simulacion.setEstado(EstadoSimulacion.BORRADOR);
        return toResponse(simulacionRepository.save(simulacion));
    }

    /**
     * Lo que le falta a la simulación para poder jugarse (vacío = lista):
     * al menos un caso, el primer caso con balance completo que cuadre, al menos una empresa con integrantes
     * y todas las empresas con integrantes con líder.
     */
    List<ValidacionException.Problema> problemasParaArrancar(Simulacion simulacion) {
        List<ValidacionException.Problema> problemas = new ArrayList<>();

        List<Caso> casos = casoRepository.findByIdSimulacion(simulacion.getId());
        if (casos.isEmpty()) {
            problemas.add(new ValidacionException.Problema("casos", "La simulación no tiene casos"));
        } else {
            Caso primero = OrdenCasos.primero(casos).orElseThrow();
            EstadoFinanciero ef = primero.getFinanciero();
            if (ef == null || !ef.partidasFaltantes().isEmpty() || !ef.cuadra()) {
                problemas.add(new ValidacionException.Problema("casos", String.format(
                        "El primer caso («%s») no tiene un balance inicial completo que cuadre",
                        primero.getNombreEmpresa())));
            }
        }

        boolean algunaConIntegrantes = false;
        List<String> sinLider = new ArrayList<>();
        for (Empresa e : empresaRepository.findByIdSimulacion(simulacion.getId())) {
            List<Integrante> integrantes = integranteRepository.findByIdEmpresa(e.getId());
            if (integrantes.isEmpty()) continue;
            algunaConIntegrantes = true;
            if (integrantes.stream().noneMatch(Integrante::isEsLider)) {
                sinLider.add(e.getCodigoEmpresa() + " (" + e.getNombre() + ")");
            }
        }
        if (!algunaConIntegrantes) {
            problemas.add(new ValidacionException.Problema("empresas", "Ninguna empresa tiene integrantes"));
        }
        if (!sinLider.isEmpty()) {
            problemas.add(new ValidacionException.Problema("empresas",
                    "Estas empresas no tienen líder: " + String.join(", ", sinLider)));
        }
        return problemas;
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
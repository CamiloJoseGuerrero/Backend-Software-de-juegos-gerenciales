package com.estratego.infrastructure.scheduling;

import com.estratego.application.event.EstadosFinancierosPendientesEvent;
import com.estratego.application.usecase.CalculoEstadosService;
import com.estratego.application.usecase.SimulacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CierreAutomaticoCasos {

    private final CalculoEstadosService calculoEstadosService;
    private final SimulacionService simulacionService;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${app.cierre-casos.intervalo-ms:60000}",
               initialDelayString = "${app.cierre-casos.retraso-inicial-ms:30000}")
    public void revisarSimulacionesEnCurso() {
        Set<Long> porProcesar = new LinkedHashSet<>();
        try {
            List<Long> cambiadas = simulacionService.avanzarAutomaticamente(LocalDate.now(clock));
            if (!cambiadas.isEmpty()) log.info("Simulaciones que cambiaron de estado solas: {}", cambiadas);
            porProcesar.addAll(cambiadas);
        } catch (RuntimeException ex) {
            log.error("No se pudo avanzar el estado de las simulaciones", ex);
        }
        porProcesar.addAll(calculoEstadosService.simulacionesEnCurso());
        for (Long id : porProcesar) {
            procesar(id);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alCambiarElJuego(EstadosFinancierosPendientesEvent event) {
        procesar(event.idSimulacion());
    }

    private void procesar(Long idSimulacion) {
        try {
            calculoEstadosService.procesar(idSimulacion);
        } catch (RuntimeException ex) {
            log.error("No se pudieron calcular los estados financieros de la simulación {}", idSimulacion, ex);
        }
    }
}

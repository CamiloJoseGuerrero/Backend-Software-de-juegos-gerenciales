package com.estratego.infrastructure.scheduling;

import com.estratego.application.event.EstadosFinancierosPendientesEvent;
import com.estratego.application.usecase.CalculoEstadosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Cierre automático de casos (D2): cuando pasa la fecha de fin de un caso, calcula los estados FINAL
 * de las empresas (con su decisión o con la penalización) sin que el docente haga nada.
 * También siembra los INICIAL de empresas que se agreguen con el caso abierto.
 *
 * <p>Además reacciona de inmediato (después del commit) cuando se activa un caso o se inicia/finaliza
 * una simulación. Cada simulación se procesa en su propia transacción: un error en una no frena a las demás.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CierreAutomaticoCasos {

    private final CalculoEstadosService calculoEstadosService;

    @Scheduled(fixedDelayString = "${app.cierre-casos.intervalo-ms:60000}",
               initialDelayString = "${app.cierre-casos.retraso-inicial-ms:30000}")
    public void revisarSimulacionesEnCurso() {
        for (Long id : calculoEstadosService.simulacionesEnCurso()) {
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

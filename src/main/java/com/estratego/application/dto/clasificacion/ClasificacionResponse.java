package com.estratego.application.dto.clasificacion;

import com.estratego.domain.model.simulacion.EstadoSimulacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Clasificación por utilidad neta acumulada.
 * {@code clasificacion} vacía (y {@code casosConsiderados = 0}) = todavía no hay resultados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionResponse {

    private Long idSimulacion;
    private String nombreSimulacion;
    private EstadoSimulacion estadoSimulacion;
    /** true cuando la simulación está FINALIZADA: el ranking ya no cambia. */
    private boolean definitiva;
    /** Casos que entran en el cálculo: activados y con la partida terminada (o todos los activados si la simulación finalizó). */
    private int casosConsiderados;
    private List<FilaClasificacionResponse> clasificacion;
}

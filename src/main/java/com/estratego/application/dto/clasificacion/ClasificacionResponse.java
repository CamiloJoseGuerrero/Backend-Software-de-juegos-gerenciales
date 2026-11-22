package com.estratego.application.dto.clasificacion;

import com.estratego.domain.model.simulacion.EstadoSimulacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionResponse {

    private Long idSimulacion;
    private String nombreSimulacion;
    private EstadoSimulacion estadoSimulacion;
    private boolean definitiva;
    private int casosConsiderados;
    private List<FilaClasificacionResponse> clasificacion;
}

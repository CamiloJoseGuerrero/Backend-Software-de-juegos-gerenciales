package com.estratego.application.dto.clasificacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilaClasificacionResponse {

    /** 1 = primera. Empate = misma posición (1, 1, 3). */
    private int posicion;
    private Long idEmpresa;
    private String codigoEmpresa;
    private String nombreEmpresa;
    private BigDecimal utilidadAcumulada;
    private int casosSinDecision;
    /** Un elemento por caso considerado, en orden de partida. */
    private List<ResultadoCasoResponse> desglose;
}

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

    private int posicion;
    private Long idEmpresa;
    private String codigoEmpresa;
    private String nombreEmpresa;
    private BigDecimal utilidadAcumulada;
    private int casosSinDecision;
    private List<ResultadoCasoResponse> desglose;
}

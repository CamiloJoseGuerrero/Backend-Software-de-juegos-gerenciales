package com.estratego.application.dto.clasificacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoCasoResponse {

    private Long idCaso;
    private String nombreCaso;
    private LocalDateTime fechaFinPartida;
    private boolean decidio;
    private Long idOpcion;
    private String opcionElegida;
    private BigDecimal utilidadBase;
    private BigDecimal penalizacionPorcentaje;
    private BigDecimal utilidadDelCaso;
}

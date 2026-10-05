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
    /** El campo "nombre" del caso (el nombre de la empresa del caso, p. ej. "TextilAndes S.A."). */
    private String nombreCaso;
    private LocalDateTime fechaFinPartida;
    private boolean decidio;
    /** null si no decidió. */
    private Long idOpcion;
    /** null si no decidió. */
    private String opcionElegida;
    /** Utilidad neta del caso tal como la escribió el docente. */
    private BigDecimal utilidadBase;
    /** % aplicado si no decidió (penalizacionMax del caso); null si decidió. */
    private BigDecimal penalizacionPorcentaje;
    /** Lo que suma a la utilidad acumulada: con el impacto de la opción, o penalizada. */
    private BigDecimal utilidadDelCaso;
}

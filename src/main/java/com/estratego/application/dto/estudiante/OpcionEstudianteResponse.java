package com.estratego.application.dto.estudiante;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Opción tal como la ve el estudiante: sin el Resultado. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpcionEstudianteResponse {

    private Long id;
    private Integer orden;
    private String opcion;
}

package com.estratego.application.dto.estudiante;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** La decisión de la empresa: la opción elegida y su Resultado. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResponse {

    private Long idCaso;
    private Long idEmpresa;
    private Long idOpcion;
    private String opcion;
    private String resultado;
    private String decididaPor;
    private LocalDateTime fechaDecision;
}

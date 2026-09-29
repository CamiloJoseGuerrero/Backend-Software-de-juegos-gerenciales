package com.estratego.application.dto.docente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Para el docente: qué eligió cada empresa de la simulación en un caso (null si aún no decide). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionEmpresaResponse {

    private Long idEmpresa;
    private String codigoEmpresa;
    private String nombreEmpresa;
    private boolean decidio;
    private Long idOpcion;
    private String opcion;
    private String resultado;
    private String decididaPor;
    private LocalDateTime fechaDecision;
}

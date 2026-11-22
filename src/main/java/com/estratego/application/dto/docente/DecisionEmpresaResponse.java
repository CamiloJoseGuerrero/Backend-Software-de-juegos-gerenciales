package com.estratego.application.dto.docente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

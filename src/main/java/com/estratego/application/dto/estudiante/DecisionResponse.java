package com.estratego.application.dto.estudiante;

import com.estratego.domain.model.caso.ImpactoOpcion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResponse {

    private Long idCaso;
    private Long idEmpresa;
    private Long idOpcion;
    private String opcion;
    private String resultado;
    private ImpactoOpcion impacto;
    private String decididaPor;
    private LocalDateTime fechaDecision;
}

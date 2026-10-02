package com.estratego.application.dto.docente;

import com.estratego.domain.model.caso.ImpactoOpcion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpcionCasoResponse {

    private Long id;
    private Integer orden;
    private String opcion;
    private String resultado;
    private ImpactoOpcion impacto;
}

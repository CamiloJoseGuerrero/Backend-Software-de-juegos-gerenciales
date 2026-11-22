package com.estratego.domain.model.caso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpcionCaso {

    private Long id;
    private Integer orden;
    private String opcion;
    private String resultado;
    private ImpactoOpcion impacto;

    public OpcionCaso(Long id, Integer orden, String opcion, String resultado) {
        this(id, orden, opcion, resultado, null);
    }
}

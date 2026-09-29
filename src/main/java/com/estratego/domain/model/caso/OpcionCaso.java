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
    /** Nombre literal del cliente: "Opción". */
    private String opcion;
    /** Nombre literal del cliente: "Resultado". */
    private String resultado;
}

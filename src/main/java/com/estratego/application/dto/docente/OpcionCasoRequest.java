package com.estratego.application.dto.docente;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpcionCasoRequest {

    @NotBlank(message = "Cada opción debe tener el texto de la Opción")
    private String opcion;

    @NotBlank(message = "Cada opción debe tener su Resultado")
    private String resultado;

    /** Opcional: efecto financiero de elegir esta opción. */
    @Valid
    private ImpactoOpcionRequest impacto;

    public OpcionCasoRequest(String opcion, String resultado) {
        this(opcion, resultado, null);
    }
}

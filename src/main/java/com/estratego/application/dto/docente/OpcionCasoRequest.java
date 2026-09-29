package com.estratego.application.dto.docente;

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
}

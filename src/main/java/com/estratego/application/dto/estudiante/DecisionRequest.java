package com.estratego.application.dto.estudiante;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionRequest {

    @NotNull(message = "Debe indicar el caso")
    private Long idCaso;

    @NotNull(message = "Debe elegir una opción")
    private Long idOpcion;
}

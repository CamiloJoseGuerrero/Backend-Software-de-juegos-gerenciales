package com.estratego.application.dto.docente;

import com.estratego.domain.model.integrante.Departamento;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarIntegranteRequest {

    @NotNull(message = "El departamento es obligatorio")
    private Departamento departamento;
}

package com.estratego.application.dto.docente;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearEquipoRequest {

    @NotEmpty(message = "Debe incluir al menos 1 estudiante")
    @Size(min = 1, max = 4, message = "El equipo debe tener entre 1 y 4 estudiantes")
    private List<Long> estudianteIds;

    @NotNull(message = "Debe designar un líder")
    private Long liderId;
}
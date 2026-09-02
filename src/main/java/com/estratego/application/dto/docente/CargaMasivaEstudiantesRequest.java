package com.estratego.application.dto.docente;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CargaMasivaEstudiantesRequest {

    @NotEmpty(message = "La lista de estudiantes no puede estar vacía")
    @Valid
    private List<EstudianteCargaRequest> estudiantes;

}

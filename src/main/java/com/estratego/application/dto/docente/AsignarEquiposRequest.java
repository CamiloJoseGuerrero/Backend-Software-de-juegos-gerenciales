package com.estratego.application.dto.docente;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarEquiposRequest {

    @NotEmpty(message = "Debe incluir al menos un equipo")
    private List<Long> equipoIds;
}
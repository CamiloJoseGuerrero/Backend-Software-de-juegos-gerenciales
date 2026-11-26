package com.estratego.application.dto.docente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoResponse {

    private Long id;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private long cantidadEstudiantes;
    private long cantidadSimulaciones;
}

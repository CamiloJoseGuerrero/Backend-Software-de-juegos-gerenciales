package com.estratego.domain.model.curso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    private Long id;
    private Long idDocente;
    private String nombre;
    private LocalDateTime fechaCreacion;
}

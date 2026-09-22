package com.estratego.domain.model.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profesor {

    private Long idProfesor;        
    private String departamento;
    private String tituloAcademico;
}
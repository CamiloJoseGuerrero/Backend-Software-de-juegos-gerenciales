package com.estratego.domain.model.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estudiante {

    private Long idEstudiante;      
    private String codigoEstudiantil;
    private String carrera;
    private Integer semestre;
    private Integer edad;           
    private String genero;          
}
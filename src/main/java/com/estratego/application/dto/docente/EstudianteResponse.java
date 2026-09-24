package com.estratego.application.dto.docente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstudianteResponse {

    private Long id;
    private String nombre;
    private String correo;
    private String numeroIdentificacion;
    private Integer edad;
    private String genero;
    private String usuario;
}
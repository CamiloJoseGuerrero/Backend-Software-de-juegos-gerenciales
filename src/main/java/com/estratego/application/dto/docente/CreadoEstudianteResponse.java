package com.estratego.application.dto.docente;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreadoEstudianteResponse {

    private Long id;
    private String nombre;
    private String correo;
    private String numeroIdentificacion;
    private Integer edad;
    private String genero;
    private String rol;
    @JsonIgnore
    private boolean creado;
    private LocalDateTime cargadoEn;
}
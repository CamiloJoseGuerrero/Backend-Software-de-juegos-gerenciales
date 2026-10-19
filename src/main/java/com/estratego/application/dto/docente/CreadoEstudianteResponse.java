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
    /**
     * true si se creó la cuenta (201), false si ya existía y solo se vinculó (200).
     * No viaja en el JSON: la contraseña inicial ya no se devuelve, llega al estudiante por correo.
     */
    @JsonIgnore
    private boolean creado;
    private LocalDateTime cargadoEn;
}
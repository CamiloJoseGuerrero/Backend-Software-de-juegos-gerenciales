package com.estratego.application.dto.docente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorCargaResponse {

    private int fila;
    private String correo;
    private String numeroIdentificacion;
    private String mensaje;

}

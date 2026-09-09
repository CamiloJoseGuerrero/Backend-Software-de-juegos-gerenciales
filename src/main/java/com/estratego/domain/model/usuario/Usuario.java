package com.estratego.domain.model.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long id;
    private String nombre;
    private String correo;
    private String numeroIdentificacion;
    private String contrasena;
    private Rol rol;

}

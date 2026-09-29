package com.estratego.application.event;

import java.util.List;

/** Se publica al terminar una carga masiva con los estudiantes creados y su contraseña inicial. */
public record EstudiantesCargadosEvent(List<Credencial> credenciales) {

    public record Credencial(String nombre, String correo, String usuario, String contrasena) {
    }
}

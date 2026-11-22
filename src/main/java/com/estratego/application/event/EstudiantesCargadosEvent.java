package com.estratego.application.event;

import java.util.List;

public record EstudiantesCargadosEvent(List<Credencial> credenciales) {

    public record Credencial(String nombre, String correo, String usuario, String contrasena) {
    }
}

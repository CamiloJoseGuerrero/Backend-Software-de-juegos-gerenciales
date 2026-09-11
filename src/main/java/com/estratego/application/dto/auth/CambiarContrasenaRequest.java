package com.estratego.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarContrasenaRequest {

    @NotBlank(message = "La contraseña actual es requerida")
    private String contrasenaActual;

    @NotBlank(message = "La nueva contraseña es requerida")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,72}$",
        message = "La contraseña debe tener entre 8 y 72 caracteres, con mayúscula, minúscula, número y símbolo"
    )
    private String contrasenaNueva;
}   
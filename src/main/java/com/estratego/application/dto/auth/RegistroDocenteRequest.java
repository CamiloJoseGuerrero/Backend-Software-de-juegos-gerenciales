package com.estratego.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDocenteRequest {

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El correo es requerido")
    @Email(message = "El correo debe ser válido")
    private String correo;

    @NotBlank(message = "El número de identificación es requerido")
    private String numeroIdentificacion;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    // Símbolo = cualquier carácter que no sea letra, número ni espacio (misma regla que cambiar-contraseña)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).{8,72}$", message = "La contraseña debe tener entre 8 y 72 caracteres, con mayúscula, minúscula, número y símbolo (cualquier carácter que no sea letra, número ni espacio)")
    private String contrasena;

}
package com.estratego.application.dto.docente;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Alta manual de un estudiante (mismos datos que una fila del Excel). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearEstudianteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Pattern(regexp = "^\\s*[^@\\s]+@[^@\\s]+\\.[^@\\s]+\\s*$", message = "El correo debe ser válido")
    private String correo;

    @NotBlank(message = "El número de identificación es obligatorio")
    @Size(max = 50, message = "El número de identificación no puede exceder 50 caracteres")
    private String numeroIdentificacion;

    @Min(value = 1, message = "La edad debe estar entre 1 y 120")
    @Max(value = 120, message = "La edad debe estar entre 1 y 120")
    private Integer edad;

    @Pattern(regexp = "^[MmFf]$", message = "El género debe ser M o F")
    private String genero;
}

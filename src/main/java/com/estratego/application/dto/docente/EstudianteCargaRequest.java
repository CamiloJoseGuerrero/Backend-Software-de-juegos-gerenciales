package com.estratego.application.dto.docente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstudianteCargaRequest {

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El correo es requerido")
    @Email(message = "El correo debe ser válido")
    private String correo;

    @NotBlank(message = "El número de identificación es requerido")
    private String numeroIdentificacion;

}

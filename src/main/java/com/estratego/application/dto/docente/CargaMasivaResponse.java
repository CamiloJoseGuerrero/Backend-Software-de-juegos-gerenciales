package com.estratego.application.dto.docente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CargaMasivaResponse {

    /** Estudiantes nuevos: se creó la cuenta y se envía la contraseña por correo. */
    private List<CreadoEstudianteResponse> creados;

    /** Estudiantes que ya tenían cuenta (de otro docente): solo se agregaron a tu lista. */
    private List<EstudianteResponse> vinculados;

    private List<ErrorCargaResponse> errores;
}

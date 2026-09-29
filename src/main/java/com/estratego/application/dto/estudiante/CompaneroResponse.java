package com.estratego.application.dto.estudiante;

import com.estratego.domain.model.integrante.Departamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompaneroResponse {

    private Long idUsuario;
    private String nombre;
    private String correo;
    private Departamento departamento;
    private boolean esLider;
}

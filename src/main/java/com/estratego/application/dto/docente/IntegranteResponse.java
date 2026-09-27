package com.estratego.application.dto.docente;

import com.estratego.domain.model.integrante.Departamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegranteResponse {

    private Long id;
    private Long idEmpresa;
    private Long idUsuario;
    private String nombre;
    private String correo;
    private String numeroIdentificacion;
    private Departamento departamento;
    private boolean esLider;
}

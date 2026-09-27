package com.estratego.domain.model.integrante;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Integrante {

    private Long id;
    private Long idEmpresa;
    private Long idUsuario;
    private Departamento departamento;
    private boolean esLider;
}

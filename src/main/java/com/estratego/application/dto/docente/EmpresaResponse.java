package com.estratego.application.dto.docente;

import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponse {

    private Long id;
    private Long idSimulacion;
    private String codigoEmpresa;
    private String nombre;
    private String estrategia;
    private TipoJugador tipoJugador;
    private EstadoEmpresa estado;
}
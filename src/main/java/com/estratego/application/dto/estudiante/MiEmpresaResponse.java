package com.estratego.application.dto.estudiante;

import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiEmpresaResponse {

    private Long id;
    private Long idSimulacion;
    private String codigoEmpresa;
    private String nombre;
    private String estrategia;
    private TipoJugador tipoJugador;
    private EstadoEmpresa estado;
    private List<CompaneroResponse> integrantes;
}

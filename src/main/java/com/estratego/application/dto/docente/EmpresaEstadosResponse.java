package com.estratego.application.dto.docente;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmpresaEstadosResponse(
        Long idEmpresa,
        String codigoEmpresa,
        String nombre,
        EstadoFinancieroEmpresaResponse inicial,
        @JsonProperty("final")
        EstadoFinancieroEmpresaResponse finalEstado
) {
}

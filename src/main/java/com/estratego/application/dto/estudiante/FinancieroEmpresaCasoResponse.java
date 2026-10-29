package com.estratego.application.dto.estudiante;

import com.estratego.application.dto.docente.EstadoFinancieroEmpresaResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public record FinancieroEmpresaCasoResponse(
        EstadoFinancieroEmpresaResponse inicial,
        @JsonProperty("final")
        EstadoFinancieroEmpresaResponse finalEstado
) {
}

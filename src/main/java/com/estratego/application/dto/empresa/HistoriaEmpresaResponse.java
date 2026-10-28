package com.estratego.application.dto.empresa;

import com.estratego.application.dto.docente.EstadoFinancieroEmpresaResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record HistoriaEmpresaResponse(
        Long idCaso,
        String nombreCaso,
        LocalDateTime fechaInicioPartida,
        EstadoFinancieroEmpresaResponse inicial,
        @JsonProperty("final")
        EstadoFinancieroEmpresaResponse finalEstado
) {
}

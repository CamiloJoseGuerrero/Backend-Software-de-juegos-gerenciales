package com.estratego.domain.model.caso;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TipoImpacto {
    @JsonProperty("porcentaje") PORCENTAJE,
    @JsonProperty("monto") MONTO
}

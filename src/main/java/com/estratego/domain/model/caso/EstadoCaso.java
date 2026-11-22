package com.estratego.domain.model.caso;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EstadoCaso {
    @JsonProperty("borrador") BORRADOR,
    @JsonProperty("activo") ACTIVO
}

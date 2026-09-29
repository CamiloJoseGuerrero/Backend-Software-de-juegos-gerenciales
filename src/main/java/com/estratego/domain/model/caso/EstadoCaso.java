package com.estratego.domain.model.caso;

import com.fasterxml.jackson.annotation.JsonProperty;

/** En el JSON viaja en minúscula, como lo usa el front ('borrador' | 'activo'). */
public enum EstadoCaso {
    @JsonProperty("borrador") BORRADOR,
    @JsonProperty("activo") ACTIVO
}

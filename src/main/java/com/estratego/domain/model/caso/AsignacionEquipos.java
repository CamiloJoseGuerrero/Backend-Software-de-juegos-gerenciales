package com.estratego.domain.model.caso;

import com.fasterxml.jackson.annotation.JsonProperty;

/** En el JSON viaja en minúscula, como lo usa el front ('manual' | 'automatica'). */
public enum AsignacionEquipos {
    @JsonProperty("manual") MANUAL,
    @JsonProperty("automatica") AUTOMATICA
}

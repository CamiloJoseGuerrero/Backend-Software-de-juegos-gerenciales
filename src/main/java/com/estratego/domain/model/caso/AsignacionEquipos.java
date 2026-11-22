package com.estratego.domain.model.caso;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AsignacionEquipos {
    @JsonProperty("manual") MANUAL,
    @JsonProperty("automatica") AUTOMATICA
}

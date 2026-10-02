package com.estratego.domain.model.caso;

import com.fasterxml.jackson.annotation.JsonProperty;

/** En el JSON viaja en minúscula ('porcentaje' | 'monto'). */
public enum TipoImpacto {
    @JsonProperty("porcentaje") PORCENTAJE,
    @JsonProperty("monto") MONTO
}

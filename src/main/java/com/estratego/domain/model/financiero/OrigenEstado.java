package com.estratego.domain.model.financiero;

/** De dónde salió un estado financiero de empresa. */
public enum OrigenEstado {
    /** INICIAL: balance que definió el docente en el primer caso (también empresas que entran tarde). */
    DOCENTE,
    /** INICIAL: balance FINAL de la empresa en el caso anterior. */
    HEREDADO,
    /** FINAL: se aplicó el impacto de la opción elegida. */
    DECISION,
    /** FINAL: la empresa no decidió; se aplicó la penalización del caso. */
    PENALIZACION
}

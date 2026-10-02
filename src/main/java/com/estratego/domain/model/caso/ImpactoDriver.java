package com.estratego.domain.model.caso;

import java.math.BigDecimal;

/** Cuánto cambia un rubro: positivo sube, negativo baja. */
public record ImpactoDriver(TipoImpacto tipo, BigDecimal valor) {
}

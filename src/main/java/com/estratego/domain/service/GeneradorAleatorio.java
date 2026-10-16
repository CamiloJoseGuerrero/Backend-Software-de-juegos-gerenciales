package com.estratego.domain.service;

import java.math.BigDecimal;

/**
 * Fuente de azar del juego. Interfaz para no usar Math.random() en el dominio
 * y poder fijar los resultados en los tests.
 */
public interface GeneradorAleatorio {

    /** Un valor uniforme entre min y max (ambos incluidos), con 2 decimales. Si min es null o mayor que max, devuelve max. */
    BigDecimal entre(BigDecimal min, BigDecimal max);
}

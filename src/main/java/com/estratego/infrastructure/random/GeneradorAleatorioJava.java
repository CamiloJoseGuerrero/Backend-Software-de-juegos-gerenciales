package com.estratego.infrastructure.random;

import com.estratego.domain.service.GeneradorAleatorio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

/** Azar de la JVM: centésimas uniformes entre min y max, ambos incluidos. */
@Component
public class GeneradorAleatorioJava implements GeneradorAleatorio {

    @Override
    public BigDecimal entre(BigDecimal min, BigDecimal max) {
        if (max == null) return BigDecimal.ZERO;
        if (min == null || min.compareTo(max) >= 0) return max.setScale(2, RoundingMode.HALF_UP);
        long desde = min.movePointRight(2).setScale(0, RoundingMode.CEILING).longValueExact();
        long hasta = max.movePointRight(2).setScale(0, RoundingMode.FLOOR).longValueExact();
        if (desde > hasta) return max.setScale(2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(desde, hasta + 1), 2);
    }
}

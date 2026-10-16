package com.estratego.infrastructure;

import com.estratego.infrastructure.random.GeneradorAleatorioJava;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorAleatorioJavaTest {

    private final GeneradorAleatorioJava generador = new GeneradorAleatorioJava();

    @Test
    void siempreQuedaDentroDelRangoConDosDecimales() {
        BigDecimal min = new BigDecimal("2"), max = new BigDecimal("8");
        for (int i = 0; i < 2000; i++) {
            BigDecimal v = generador.entre(min, max);
            assertTrue(v.compareTo(min) >= 0 && v.compareTo(max) <= 0, "fuera de rango: " + v);
            assertEquals(2, v.scale());
        }
    }

    @Test
    void sinRangoDevuelveElMaximo() {
        assertEquals(new BigDecimal("8.00"), generador.entre(null, new BigDecimal("8")));
        assertEquals(new BigDecimal("5.00"), generador.entre(new BigDecimal("5"), new BigDecimal("5")));
        assertEquals(new BigDecimal("3.00"), generador.entre(new BigDecimal("9"), new BigDecimal("3")));
    }
}

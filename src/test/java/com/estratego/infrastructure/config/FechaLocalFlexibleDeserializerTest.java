package com.estratego.infrastructure.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class FechaLocalFlexibleDeserializerTest {

    record Dato(LocalDateTime fecha) {
    }

    private final ObjectMapper mapper = new ObjectMapper().registerModule(
            new SimpleModule().addDeserializer(LocalDateTime.class,
                    new FechaLocalFlexibleDeserializer(ZoneId.of("America/Bogota"))));

    private LocalDateTime leer(String valor) throws Exception {
        return mapper.readValue("{\"fecha\":\"" + valor + "\"}", Dato.class).fecha();
    }

    private static final LocalDateTime ESPERADA = LocalDateTime.of(2026, 9, 29, 16, 10);

    @Test
    void horaLocalSinZonaQuedaIgual() throws Exception {
        assertEquals(ESPERADA, leer("2026-09-29T16:10:00"));
        assertEquals(ESPERADA, leer("2026-09-29T16:10"));
    }

    @Test
    void utcConZSeConvierteAHoraDeBogota() throws Exception {
        assertEquals(ESPERADA, leer("2026-09-29T21:10:00.000Z"));
        assertEquals(ESPERADA, leer("2026-09-29T21:10:00Z"));
    }

    @Test
    void desfaseExplicitoSeConvierte() throws Exception {
        assertEquals(ESPERADA, leer("2026-09-29T16:10:00-05:00"));
        assertEquals(ESPERADA, leer("2026-09-29T23:10:00+02:00"));
    }

    @Test
    void cambioDeDiaPorZonaHoraria() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 29, 22, 30), leer("2026-09-30T03:30:00Z"));
    }

    @Test
    void textoInvalidoFalla() {
        assertThrows(JsonMappingException.class, () -> leer("mañana a las 3"));
    }

    @Test
    void vacioEsNull() throws Exception {
        assertNull(leer(""));
    }
}

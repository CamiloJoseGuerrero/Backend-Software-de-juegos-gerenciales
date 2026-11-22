package com.estratego.infrastructure.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class FechaLocalFlexibleDeserializer extends StdDeserializer<LocalDateTime> {

    private final ZoneId zona;

    public FechaLocalFlexibleDeserializer(ZoneId zona) {
        super(LocalDateTime.class);
        this.zona = zona;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String texto = p.getValueAsString();
        if (texto == null || texto.isBlank()) {
            return null;
        }
        texto = texto.trim();
        try {
            return OffsetDateTime.parse(texto).atZoneSameInstant(zona).toLocalDateTime();
        } catch (DateTimeParseException sinDesfase) {
            try {
                return LocalDateTime.parse(texto);
            } catch (DateTimeParseException ex) {
                return (LocalDateTime) ctxt.handleWeirdStringValue(LocalDateTime.class, texto,
                        "Formato de fecha inválido. Use 2026-09-29T16:10:00");
            }
        }
    }
}

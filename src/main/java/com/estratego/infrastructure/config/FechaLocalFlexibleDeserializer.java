package com.estratego.infrastructure.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Lee fechas-hora del JSON en cualquiera de estos formatos y las deja en la hora local de la aplicación:
 *   "2026-09-29T16:10:00"          -> tal cual (ya es hora local)
 *   "2026-09-29T21:10:00.000Z"     -> UTC, se convierte (16:10 en Bogotá)
 *   "2026-09-29T16:10:00-05:00"    -> con desfase, se convierte
 * Así el front puede enviar new Date().toISOString() sin que las partidas queden 5 horas corridas.
 */
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

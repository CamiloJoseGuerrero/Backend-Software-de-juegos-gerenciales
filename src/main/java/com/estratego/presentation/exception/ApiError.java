package com.estratego.presentation.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private String message;
    private int status;
    private String timestamp;

    /**
     * Solo en errores de validación (400): un elemento por campo, con la ruta del body
     * (p. ej. "financiero.impuestoRenta", "opciones[0].impacto.costoVentas.valor").
     * Se omite en los demás errores.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ErrorCampo> errores;

    public ApiError(String message, int status, String timestamp) {
        this(message, status, timestamp, null);
    }

    /** {@code campo} es null cuando el error es del objeto completo y no de un campo. */
    public record ErrorCampo(String campo, String mensaje) {
    }
}

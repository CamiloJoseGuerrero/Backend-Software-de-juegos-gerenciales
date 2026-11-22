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

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ErrorCampo> errores;

    public ApiError(String message, int status, String timestamp) {
        this(message, status, timestamp, null);
    }

    public record ErrorCampo(String campo, String mensaje) {
    }
}

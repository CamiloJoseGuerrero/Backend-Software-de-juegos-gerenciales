package com.estratego.application.usecase;

/** Regla de negocio que se puede atribuir a un campo del body (responde 400 con {@code errores}). */
public class CampoInvalidoException extends IllegalArgumentException {

    private final String campo;

    public CampoInvalidoException(String campo, String message) {
        super(message);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}

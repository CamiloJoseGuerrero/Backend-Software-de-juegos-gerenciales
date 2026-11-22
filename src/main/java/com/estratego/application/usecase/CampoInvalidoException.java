package com.estratego.application.usecase;

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

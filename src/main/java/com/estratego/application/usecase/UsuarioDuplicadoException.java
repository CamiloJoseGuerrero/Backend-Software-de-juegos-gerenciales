package com.estratego.application.usecase;

public class UsuarioDuplicadoException extends RuntimeException {

    public UsuarioDuplicadoException(String message) {
        super(message);
    }
}
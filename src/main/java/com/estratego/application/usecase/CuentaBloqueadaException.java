package com.estratego.application.usecase;

public class CuentaBloqueadaException extends RuntimeException {

    public CuentaBloqueadaException(String message) {
        super(message);
    }
}
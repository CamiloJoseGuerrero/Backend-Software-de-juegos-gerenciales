package com.estratego.application.usecase;

import java.util.List;

public class ValidacionException extends IllegalArgumentException {

    public record Problema(String campo, String mensaje) {
    }

    private final List<Problema> problemas;

    public ValidacionException(String message, List<Problema> problemas) {
        super(message);
        this.problemas = List.copyOf(problemas);
    }

    public List<Problema> getProblemas() {
        return problemas;
    }
}

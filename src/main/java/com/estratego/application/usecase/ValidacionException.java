package com.estratego.application.usecase;

import java.util.List;

/**
 * Varias reglas de negocio incumplidas a la vez, cada una atribuible a un campo
 * (responde 400 con todas en {@code errores}). Para un solo campo, ver {@link CampoInvalidoException}.
 */
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

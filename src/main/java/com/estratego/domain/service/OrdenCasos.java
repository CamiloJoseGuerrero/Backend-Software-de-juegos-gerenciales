package com.estratego.domain.service;

import com.estratego.domain.model.caso.Caso;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Orden de los casos dentro de su simulación (caso = período).
 * Van por fecha de inicio de la partida; si empatan, el creado antes (id menor).
 * Un caso todavía sin id (se está creando) va después de los que empatan con él.
 * Solo el primero define el balance inicial: los siguientes lo heredan por empresa (D2).
 */
public final class OrdenCasos {

    public static final Comparator<Caso> ORDEN = Comparator
            .comparing(Caso::getFechaInicio, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(c -> c.getId() != null ? c.getId() : Long.MAX_VALUE);

    private OrdenCasos() {
    }

    public static Optional<Caso> primero(Collection<Caso> casos) {
        return casos.stream().min(ORDEN);
    }

    public static List<Caso> ordenados(Collection<Caso> casos) {
        return casos.stream().sorted(ORDEN).toList();
    }
}

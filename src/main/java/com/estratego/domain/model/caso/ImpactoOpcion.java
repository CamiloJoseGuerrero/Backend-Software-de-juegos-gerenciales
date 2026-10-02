package com.estratego.domain.model.caso;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Efecto financiero de elegir una opción. Cada rubro es opcional (null = no cambia).
 * Por ahora solo se guarda; lo aplicará el motor de cálculo cuando exista.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImpactoOpcion(
        ImpactoDriver ventasNetas,
        ImpactoDriver costoVentas,
        ImpactoDriver gastosAdministracion,
        ImpactoDriver gastosVentas,
        ImpactoDriver gastosFinancieros,
        ImpactoDriver impuestoRenta) {

    public boolean vacio() {
        return ventasNetas == null && costoVentas == null && gastosAdministracion == null
                && gastosVentas == null && gastosFinancieros == null && impuestoRenta == null;
    }
}

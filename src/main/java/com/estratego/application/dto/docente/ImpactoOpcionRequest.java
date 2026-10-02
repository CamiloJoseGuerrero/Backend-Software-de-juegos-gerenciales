package com.estratego.application.dto.docente;

import com.estratego.domain.model.caso.ImpactoOpcion;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Efecto financiero de una opción: hasta 6 rubros, todos opcionales.
 * Una clave desconocida (p. ej. "ventasNeta") se rechaza en vez de perderse en silencio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImpactoOpcionRequest {

    @Valid private ImpactoDriverRequest ventasNetas;
    @Valid private ImpactoDriverRequest costoVentas;
    @Valid private ImpactoDriverRequest gastosAdministracion;
    @Valid private ImpactoDriverRequest gastosVentas;
    @Valid private ImpactoDriverRequest gastosFinancieros;
    @Valid private ImpactoDriverRequest impuestoRenta;

    @JsonIgnore
    private List<String> desconocidos = new ArrayList<>();

    public ImpactoOpcionRequest(ImpactoDriverRequest ventasNetas, ImpactoDriverRequest costoVentas,
                                ImpactoDriverRequest gastosAdministracion, ImpactoDriverRequest gastosVentas,
                                ImpactoDriverRequest gastosFinancieros, ImpactoDriverRequest impuestoRenta) {
        this(ventasNetas, costoVentas, gastosAdministracion, gastosVentas, gastosFinancieros, impuestoRenta,
                new ArrayList<>());
    }

    @JsonAnySetter
    public void claveDesconocida(String nombre, Object valor) {
        desconocidos.add(nombre);
    }

    @JsonIgnore
    @AssertTrue(message = "El impacto solo acepta: ventasNetas, costoVentas, gastosAdministracion, "
            + "gastosVentas, gastosFinancieros, impuestoRenta")
    public boolean isSoloDriversConocidos() {
        return desconocidos == null || desconocidos.isEmpty();
    }

    /** null si no trae ningún rubro. */
    public ImpactoOpcion aDominio() {
        ImpactoOpcion i = new ImpactoOpcion(
                ventasNetas != null ? ventasNetas.aDominio() : null,
                costoVentas != null ? costoVentas.aDominio() : null,
                gastosAdministracion != null ? gastosAdministracion.aDominio() : null,
                gastosVentas != null ? gastosVentas.aDominio() : null,
                gastosFinancieros != null ? gastosFinancieros.aDominio() : null,
                impuestoRenta != null ? impuestoRenta.aDominio() : null);
        return i.vacio() ? null : i;
    }
}

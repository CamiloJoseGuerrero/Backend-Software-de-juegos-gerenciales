package com.estratego.application.dto.docente;

import com.estratego.domain.model.caso.ImpactoDriver;
import com.estratego.domain.model.caso.TipoImpacto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** { tipo: 'porcentaje' | 'monto', valor } — positivo sube, negativo baja. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImpactoDriverRequest {

    private static final BigDecimal MENOS_CIEN = new BigDecimal("-100");

    @NotNull(message = "Cada impacto debe indicar su tipo: 'porcentaje' o 'monto'")
    private TipoImpacto tipo;

    @NotNull(message = "Cada impacto debe indicar su valor")
    private BigDecimal valor;

    @JsonIgnore
    @AssertTrue(message = "Un impacto en porcentaje no puede ser menor que -100")
    public boolean isPorcentajeValido() {
        return tipo != TipoImpacto.PORCENTAJE || valor == null || valor.compareTo(MENOS_CIEN) >= 0;
    }

    public ImpactoDriver aDominio() {
        return new ImpactoDriver(tipo, valor);
    }
}

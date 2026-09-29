package com.estratego.application.dto.docente;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Información financiera inicial del caso ("período 0"). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancieroCaso {

    @NotNull(message = "El activo total es obligatorio")
    @DecimalMin(value = "0", message = "El activo total no puede ser negativo")
    private BigDecimal activoTotal;

    @NotNull(message = "El pasivo total es obligatorio")
    @DecimalMin(value = "0", message = "El pasivo total no puede ser negativo")
    private BigDecimal pasivoTotal;

    @NotNull(message = "El patrimonio es obligatorio")
    private BigDecimal patrimonio;

    @NotNull(message = "La utilidad neta es obligatoria")
    private BigDecimal utilidadNeta;
}

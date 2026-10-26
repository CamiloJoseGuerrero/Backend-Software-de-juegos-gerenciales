package com.estratego.application.dto.docente;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoletinCasoRequest {

    @NotNull(message = "La inflación es obligatoria")
    @DecimalMin(value = "-100.0", message = "La inflación va de -100 a 1000")
    @DecimalMax(value = "1000.0", message = "La inflación va de -100 a 1000")
    private BigDecimal inflacion;

    @NotNull(message = "La tasa de interés es obligatoria")
    @DecimalMin(value = "0.0", message = "La tasa de interés va de 0 a 1000")
    @DecimalMax(value = "1000.0", message = "La tasa de interés va de 0 a 1000")
    private BigDecimal tasaInteres;

    @NotNull(message = "La tasa de cambio es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La tasa de cambio tiene que ser mayor que 0")
    private BigDecimal tasaCambio;

    @Size(max = 2000, message = "La nota no puede superar los 2000 caracteres")
    private String nota;
}

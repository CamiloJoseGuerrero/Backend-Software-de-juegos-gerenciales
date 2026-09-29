package com.estratego.application.dto.docente;

import com.estratego.domain.model.caso.AsignacionEquipos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Crear y editar un caso (el PUT reemplaza todo, opciones incluidas).
 * Nombres de campos iguales a caso.model.ts del front. El estado no se envía: se cambia con /activar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoRequest {

    /** Solo lo usa POST /docente/casos; en /simulaciones/{id}/casos se toma de la ruta. */
    private Long idSimulacion;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    @Size(max = 100, message = "El tipo no puede exceder 100 caracteres")
    private String tipo;

    private String mision;

    private String vision;

    @NotNull(message = "La información financiera es obligatoria")
    @Valid
    private FinancieroCaso financiero;

    @NotNull(message = "La penalización mínima es obligatoria")
    @DecimalMin(value = "0", message = "La penalización mínima no puede ser negativa")
    @DecimalMax(value = "100", message = "La penalización mínima no puede superar 100%")
    private BigDecimal penalizacionMin;

    @NotNull(message = "La penalización máxima es obligatoria")
    @DecimalMin(value = "0", message = "La penalización máxima no puede ser negativa")
    @DecimalMax(value = "100", message = "La penalización máxima no puede superar 100%")
    private BigDecimal penalizacionMax;

    @NotNull(message = "La fecha de visualización es obligatoria")
    private LocalDateTime fechaVisualizacion;

    @NotNull(message = "La fecha de inicio de la partida es obligatoria")
    private LocalDateTime fechaInicioPartida;

    @NotNull(message = "La fecha de fin de la partida es obligatoria")
    private LocalDateTime fechaFinPartida;

    /** 'manual' | 'automatica'. Por defecto 'manual'. */
    private AsignacionEquipos asignacionEquipos;

    @NotEmpty(message = "El caso debe tener al menos una opción")
    @Valid
    private List<OpcionCasoRequest> opciones;
}

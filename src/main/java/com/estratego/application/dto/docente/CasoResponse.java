package com.estratego.application.dto.docente;

import com.estratego.domain.model.caso.AsignacionEquipos;
import com.estratego.domain.model.caso.EstadoCaso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Mismos nombres que caso.model.ts del front, más idSimulacion. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoResponse {

    private Long id;
    private Long idSimulacion;
    private String nombre;
    private String tipo;
    private EstadoCaso estado;
    private String mision;
    private String vision;
    private FinancieroCaso financiero;
    private BigDecimal penalizacionMin;
    private BigDecimal penalizacionMax;
    private LocalDateTime fechaVisualizacion;
    private LocalDateTime fechaInicioPartida;
    private LocalDateTime fechaFinPartida;
    private AsignacionEquipos asignacionEquipos;
    private List<OpcionCasoResponse> opciones;
}

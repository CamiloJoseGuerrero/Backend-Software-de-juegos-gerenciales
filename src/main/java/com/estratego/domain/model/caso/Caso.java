package com.estratego.domain.model.caso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Caso {

    private Long id;
    private Long idSimulacion;

    // Información general
    private String nombreEmpresa;
    private String mision;
    private String vision;
    private String tipo;

    // Información financiera inicial (período 0)
    private BigDecimal activoTotal;
    private BigDecimal pasivoTotal;
    private BigDecimal patrimonio;
    private BigDecimal utilidadNeta;

    // Penalización por no decidir (%)
    private BigDecimal penalizacionMin;
    private BigDecimal penalizacionMax;

    // Fechas
    private LocalDateTime fechaVisualizacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private List<OpcionCaso> opciones = new ArrayList<>();

    private EstadoCaso estado = EstadoCaso.BORRADOR;
    private AsignacionEquipos asignacionEquipos = AsignacionEquipos.MANUAL;
}

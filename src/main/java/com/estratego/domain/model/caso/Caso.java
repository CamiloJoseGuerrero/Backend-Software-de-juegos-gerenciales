package com.estratego.domain.model.caso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.estratego.domain.model.financiero.EstadoFinanciero;

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

    private String nombreEmpresa;
    private String mision;
    private String vision;
    private String tipo;

    private BigDecimal activoTotal;
    private BigDecimal pasivoTotal;
    private BigDecimal patrimonio;
    private BigDecimal utilidadNeta;

    private EstadoFinanciero financiero;

    private BigDecimal penalizacionMin;
    private BigDecimal penalizacionMax;

    private LocalDateTime fechaVisualizacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private List<OpcionCaso> opciones = new ArrayList<>();

    private EstadoCaso estado = EstadoCaso.BORRADOR;
    private AsignacionEquipos asignacionEquipos = AsignacionEquipos.MANUAL;

    private LocalDateTime activadoEn;
}

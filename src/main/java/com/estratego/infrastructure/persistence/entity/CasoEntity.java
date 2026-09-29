package com.estratego.infrastructure.persistence.entity;

import com.estratego.domain.model.caso.AsignacionEquipos;
import com.estratego.domain.model.caso.EstadoCaso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Requiere db/migraciones/2026-09-28_caso.sql
@Entity
@Table(name = "caso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caso")
    private Long id;

    @Column(name = "id_simulacion", nullable = false)
    private Long idSimulacion;

    @Column(name = "nombre_empresa", nullable = false, length = 150)
    private String nombreEmpresa;

    @Column(name = "mision", columnDefinition = "text")
    private String mision;

    @Column(name = "vision", columnDefinition = "text")
    private String vision;

    @Column(name = "tipo", length = 100)
    private String tipo;

    @Column(name = "activo_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal activoTotal;

    @Column(name = "pasivo_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal pasivoTotal;

    @Column(name = "patrimonio", nullable = false, precision = 18, scale = 2)
    private BigDecimal patrimonio;

    @Column(name = "utilidad_neta", nullable = false, precision = 18, scale = 2)
    private BigDecimal utilidadNeta;

    @Column(name = "penalizacion_min", nullable = false, precision = 5, scale = 2)
    private BigDecimal penalizacionMin;

    @Column(name = "penalizacion_max", nullable = false, precision = 5, scale = 2)
    private BigDecimal penalizacionMax;

    @Column(name = "fecha_visualizacion", nullable = false)
    private LocalDateTime fechaVisualizacion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    // Requiere db/migraciones/2026-09-28b_caso_estado_y_decision.sql
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCaso estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "asignacion_equipos", nullable = false, length = 20)
    private AsignacionEquipos asignacionEquipos;
}

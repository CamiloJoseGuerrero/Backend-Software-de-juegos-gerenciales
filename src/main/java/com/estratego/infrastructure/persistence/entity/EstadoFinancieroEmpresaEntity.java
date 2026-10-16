package com.estratego.infrastructure.persistence.entity;

import com.estratego.domain.model.financiero.MomentoEstado;
import com.estratego.domain.model.financiero.OrigenEstado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Requiere db/migraciones/2026-10-09_estado_financiero_empresa.sql
@Entity
@Table(name = "estado_financiero_empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoFinancieroEmpresaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_financiero_empresa")
    private Long id;

    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;

    @Column(name = "id_caso", nullable = false)
    private Long idCaso;

    @Enumerated(EnumType.STRING)
    @Column(name = "momento", nullable = false, length = 10)
    private MomentoEstado momento;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 20)
    private OrigenEstado origen;

    @Column(name = "id_opcion")
    private Long idOpcion;

    @Column(name = "calculado_en", nullable = false)
    private LocalDateTime calculadoEn;

    /** Mismas 19 columnas que la tabla caso. */
    @Column(name = "penalizacion_porcentaje", precision = 5, scale = 2)
    private java.math.BigDecimal penalizacionPorcentaje;

    @Embedded
    private EstadoFinancieroEmbeddable estado;
}

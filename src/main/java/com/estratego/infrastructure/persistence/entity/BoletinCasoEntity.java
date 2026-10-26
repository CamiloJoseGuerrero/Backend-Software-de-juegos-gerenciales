package com.estratego.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Requiere db/migraciones/2026-10-26_boletin_caso.sql
@Entity
@Table(name = "boletin_caso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoletinCasoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boletin_caso")
    private Long id;

    @Column(name = "id_caso", nullable = false, unique = true)
    private Long idCaso;

    @Column(name = "inflacion", nullable = false, precision = 8, scale = 2)
    private BigDecimal inflacion;

    @Column(name = "tasa_interes", nullable = false, precision = 8, scale = 2)
    private BigDecimal tasaInteres;

    @Column(name = "tasa_cambio", nullable = false, precision = 12, scale = 2)
    private BigDecimal tasaCambio;

    @Column(name = "nota", columnDefinition = "text")
    private String nota;

    @Column(name = "publicado_en", nullable = false)
    private LocalDateTime publicadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}

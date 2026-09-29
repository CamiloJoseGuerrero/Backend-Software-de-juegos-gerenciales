package com.estratego.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Requiere db/migraciones/2026-09-28b_caso_estado_y_decision.sql
@Entity
@Table(name = "decision_caso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionCasoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_decision_caso")
    private Long id;

    @Column(name = "id_caso", nullable = false)
    private Long idCaso;

    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;

    @Column(name = "id_opcion", nullable = false)
    private Long idOpcion;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "fecha_decision", nullable = false)
    private LocalDateTime fechaDecision;
}

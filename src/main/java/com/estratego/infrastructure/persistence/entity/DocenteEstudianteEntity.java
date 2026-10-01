package com.estratego.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Requiere db/migraciones/2026-10-01_docente_estudiante.sql
@Entity
@Table(name = "docente_estudiante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocenteEstudianteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_docente_estudiante")
    private Long id;

    @Column(name = "id_docente", nullable = false)
    private Long idDocente;

    @Column(name = "id_estudiante", nullable = false)
    private Long idEstudiante;

    @Column(name = "fecha_vinculo", nullable = false)
    private LocalDateTime fechaVinculo;
}

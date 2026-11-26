package com.estratego.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Requiere db/migraciones/2026-11-25_curso.sql
@Entity
@Table(name = "curso_estudiante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoEstudianteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso_estudiante")
    private Long id;

    @Column(name = "id_curso", nullable = false)
    private Long idCurso;

    @Column(name = "id_estudiante", nullable = false)
    private Long idEstudiante;

    @Column(name = "fecha_vinculo", nullable = false)
    private LocalDateTime fechaVinculo;
}

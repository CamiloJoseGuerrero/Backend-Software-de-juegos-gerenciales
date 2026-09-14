package com.estratego.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "docente_id", nullable = false)
    private Long docenteId;

    @Column(name = "lider_id", nullable = false)
    private Long liderId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "equipo_estudiantes",
        joinColumns = @JoinColumn(name = "equipo_id")
    )
    @Column(name = "estudiante_id", nullable = false)
    private List<Long> estudianteIds = new ArrayList<>();
}
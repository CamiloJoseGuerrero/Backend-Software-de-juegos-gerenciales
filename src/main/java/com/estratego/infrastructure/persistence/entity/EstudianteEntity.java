package com.estratego.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estudiante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstudianteEntity {

    @Id
    @Column(name = "id_estudiante", nullable = false)
    private Long idEstudiante;

    @Column(name = "codigo_estudiantil", length = 20)
    private String codigoEstudiantil;

    @Column(length = 100)
    private String carrera;

    private Integer semestre;

    // Campos extra (los agregamos nosotros porque el cliente los pidió en el Excel)
    private Integer edad;

    @Column(length = 1)
    private String genero;
}
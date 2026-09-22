package com.estratego.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profesor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorEntity {

    @Id
    @Column(name = "id_profesor", nullable = false)
    private Long idProfesor;

    @Column(length = 100)
    private String departamento;

    @Column(name = "titulo_academico", length = 100)
    private String tituloAcademico;
}
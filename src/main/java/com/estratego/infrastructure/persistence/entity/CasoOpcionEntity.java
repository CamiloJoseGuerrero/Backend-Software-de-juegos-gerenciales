package com.estratego.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "caso_opcion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoOpcionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    private Long id;

    @Column(name = "id_caso", nullable = false)
    private Long idCaso;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "opcion", nullable = false, columnDefinition = "text")
    private String opcion;

    @Column(name = "resultado", nullable = false, columnDefinition = "text")
    private String resultado;
}

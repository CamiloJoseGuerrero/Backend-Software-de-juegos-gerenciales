package com.estratego.infrastructure.persistence.entity;

import com.estratego.domain.model.integrante.Departamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "integrante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegranteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_integrante")
    private Long id;

    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "departamento", length = 50)
    private Departamento departamento;

    @Column(name = "es_lider", nullable = false)
    private boolean esLider;
}

package com.estratego.infrastructure.persistence.entity;

import com.estratego.domain.model.empresa.EstadoEmpresa;
import com.estratego.domain.model.empresa.TipoJugador;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long id;

    @Column(name = "id_simulacion", nullable = false)
    private Long idSimulacion;

    @Column(name = "codigo_empresa", nullable = false, length = 50)
    private String codigoEmpresa;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "estrategia", columnDefinition = "text")
    private String estrategia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_jugador", length = 30)
    private TipoJugador tipoJugador;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30)
    private EstadoEmpresa estado;
}
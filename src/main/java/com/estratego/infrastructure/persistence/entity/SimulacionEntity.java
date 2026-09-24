package com.estratego.infrastructure.persistence.entity;

import com.estratego.domain.model.simulacion.EstadoSimulacion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "simulacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simulacion")
    private Long id;

    @Column(name = "id_usuario_coordinador", nullable = false)
    private Long idUsuarioCoordinador;

    @Column(name = "nombre_curso", nullable = false, length = 150)
    private String nombre;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30)
    private EstadoSimulacion estado;
}
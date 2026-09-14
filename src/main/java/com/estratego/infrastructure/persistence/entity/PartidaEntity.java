package com.estratego.infrastructure.persistence.entity;

import com.estratego.domain.model.partida.EstadoPartida;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partidas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caso_id", nullable = false)
    private Long casoId;

    @Column(name = "docente_id", nullable = false)
    private Long docenteId;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_cierre", nullable = false)
    private LocalDateTime fechaHoraCierre;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Column(name = "fecha_visualizacion")
    private LocalDateTime fechaVisualizacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPartida estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "partida_equipos",
        joinColumns = @JoinColumn(name = "partida_id")
    )
    @Column(name = "equipo_id", nullable = false)
    private List<Long> equipoIds = new ArrayList<>();
}
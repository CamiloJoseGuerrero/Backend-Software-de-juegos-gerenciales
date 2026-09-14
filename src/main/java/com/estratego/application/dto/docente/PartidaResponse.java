package com.estratego.application.dto.docente;

import com.estratego.domain.model.partida.EstadoPartida;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidaResponse {

    private Long id;
    private Long casoId;
    private Long docenteId;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraCierre;
    private Integer duracionMinutos;
    private LocalDateTime fechaVisualizacion;
    private EstadoPartida estado;
    private List<Long> equipoIds;
}
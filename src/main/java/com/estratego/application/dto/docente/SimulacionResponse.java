package com.estratego.application.dto.docente;

import com.estratego.domain.model.simulacion.EstadoSimulacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionResponse {

    private Long id;
    private Long idUsuarioCoordinador;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoSimulacion estado;
}
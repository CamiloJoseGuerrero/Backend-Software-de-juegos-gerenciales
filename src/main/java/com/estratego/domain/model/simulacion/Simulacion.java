package com.estratego.domain.model.simulacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Simulacion {

    private Long id;
    private Long idUsuarioCoordinador;
    private String nombreCurso;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoSimulacion estado;
}
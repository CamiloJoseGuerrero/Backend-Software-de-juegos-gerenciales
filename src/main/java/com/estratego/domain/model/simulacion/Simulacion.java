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
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoSimulacion estado;
    private Long idCurso;

    public Simulacion(Long id, Long idUsuarioCoordinador, String nombre,
                      LocalDate fechaInicio, LocalDate fechaFin, EstadoSimulacion estado) {
        this(id, idUsuarioCoordinador, nombre, fechaInicio, fechaFin, estado, null);
    }
}

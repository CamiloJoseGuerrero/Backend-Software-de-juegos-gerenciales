package com.estratego.application.dto.estudiante;

import com.estratego.domain.model.integrante.Departamento;
import com.estratego.domain.model.simulacion.EstadoSimulacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Una simulación en la que participa el estudiante, con la empresa en la que está. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiSimulacionResponse {

    private Long idSimulacion;
    private String nombreSimulacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoSimulacion estado;
    private Long idEmpresa;
    private String codigoEmpresa;
    private String nombreEmpresa;
    private Departamento departamento;
    private boolean esLider;
}

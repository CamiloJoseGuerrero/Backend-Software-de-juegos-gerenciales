package com.estratego.application.dto.estudiante;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** El caso activo de la simulación del estudiante, con su empresa y la decisión (si ya la tomaron). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoActualResponse {

    private Long idSimulacion;
    private String nombreSimulacion;
    private Long idEmpresa;
    private String nombreEmpresa;
    private boolean esLider;
    /** true si es líder, la partida está en curso y la empresa aún no decide. */
    private boolean puedeDecidir;
    private CasoEstudianteResponse caso;
    /** null hasta que el líder decide. */
    private DecisionResponse decision;
}

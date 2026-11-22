package com.estratego.application.dto.estudiante;

import com.estratego.application.dto.docente.BoletinCasoResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoActualResponse {

    private Long idSimulacion;
    private String nombreSimulacion;
    private Long idEmpresa;
    private String nombreEmpresa;
    private boolean esLider;
    private boolean puedeDecidir;
    private CasoEstudianteResponse caso;
    private DecisionResponse decision;
    private FinancieroEmpresaCasoResponse financieroEmpresa;
    private BoletinCasoResponse boletin;
}

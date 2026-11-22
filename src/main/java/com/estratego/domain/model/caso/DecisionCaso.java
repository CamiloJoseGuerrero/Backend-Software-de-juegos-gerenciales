package com.estratego.domain.model.caso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionCaso {

    private Long id;
    private Long idCaso;
    private Long idEmpresa;
    private Long idOpcion;
    private Long idUsuario;
    private LocalDateTime fechaDecision;
}

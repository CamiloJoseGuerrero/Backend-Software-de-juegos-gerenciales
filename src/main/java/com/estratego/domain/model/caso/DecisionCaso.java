package com.estratego.domain.model.caso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Opción que eligió una empresa (su líder) en un caso. Permanente: no se edita ni se borra. */
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

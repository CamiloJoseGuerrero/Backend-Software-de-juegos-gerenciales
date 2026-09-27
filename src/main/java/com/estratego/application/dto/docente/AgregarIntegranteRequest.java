package com.estratego.application.dto.docente;

import com.estratego.domain.model.integrante.Departamento;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgregarIntegranteRequest {

    @NotNull(message = "El id del estudiante es obligatorio")
    private Long idUsuario;

    /** Opcional. Si no llega se usa GERENCIA_GENERAL (igual que el default de la BD). */
    private Departamento departamento;

    /** Opcional. Si es true, reemplaza al líder actual. */
    private Boolean esLider;
}

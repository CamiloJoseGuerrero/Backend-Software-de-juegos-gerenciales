package com.estratego.domain.model.financiero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Estado financiero propio de una empresa en un caso (arrastre del balance, D2).
 * Hay a lo sumo uno INICIAL y uno FINAL por empresa y caso.
 * Requiere db/migraciones/2026-10-09_estado_financiero_empresa.sql.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoFinancieroEmpresa {

    private Long id;
    private Long idEmpresa;
    private Long idCaso;
    private MomentoEstado momento;
    private OrigenEstado origen;
    /** Opción aplicada; solo cuando origen = DECISION. */
    private Long idOpcion;
    private EstadoFinanciero estado;
    private LocalDateTime calculadoEn;
}

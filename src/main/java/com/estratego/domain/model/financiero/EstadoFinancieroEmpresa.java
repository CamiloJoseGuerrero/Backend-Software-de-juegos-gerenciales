package com.estratego.domain.model.financiero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    /** % sorteado entre la penalización mínima y máxima del caso; solo cuando origen = PENALIZACION. */
    private BigDecimal penalizacionPorcentaje;

    public EstadoFinancieroEmpresa(Long id, Long idEmpresa, Long idCaso, MomentoEstado momento, OrigenEstado origen,
                                   Long idOpcion, EstadoFinanciero estado, LocalDateTime calculadoEn) {
        this(id, idEmpresa, idCaso, momento, origen, idOpcion, estado, calculadoEn, null);
    }
}

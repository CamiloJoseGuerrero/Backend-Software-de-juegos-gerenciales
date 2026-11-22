package com.estratego.domain.model.financiero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoFinancieroEmpresa {

    private Long id;
    private Long idEmpresa;
    private Long idCaso;
    private MomentoEstado momento;
    private OrigenEstado origen;
    private Long idOpcion;
    private EstadoFinanciero estado;
    private LocalDateTime calculadoEn;
    private BigDecimal penalizacionPorcentaje;

    public EstadoFinancieroEmpresa(Long id, Long idEmpresa, Long idCaso, MomentoEstado momento, OrigenEstado origen,
                                   Long idOpcion, EstadoFinanciero estado, LocalDateTime calculadoEn) {
        this(id, idEmpresa, idCaso, momento, origen, idOpcion, estado, calculadoEn, null);
    }
}

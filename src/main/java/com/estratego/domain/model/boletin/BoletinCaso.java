package com.estratego.domain.model.boletin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoletinCaso {

    private Long id;
    private Long idCaso;
    private BigDecimal inflacion;
    private BigDecimal tasaInteres;
    private BigDecimal tasaCambio;
    private String nota;
    private LocalDateTime publicadoEn;
    private LocalDateTime actualizadoEn;
}

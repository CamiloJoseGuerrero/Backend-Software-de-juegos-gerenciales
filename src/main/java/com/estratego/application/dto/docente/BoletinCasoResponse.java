package com.estratego.application.dto.docente;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoletinCasoResponse {

    private Long idCaso;
    private BigDecimal inflacion;
    private BigDecimal tasaInteres;
    private BigDecimal tasaCambio;

    @JsonProperty("nota")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String nota;

    private LocalDateTime publicadoEn;
    private LocalDateTime actualizadoEn;
}

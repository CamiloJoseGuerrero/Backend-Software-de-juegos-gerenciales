package com.estratego.application.dto.estudiante;

import com.estratego.application.dto.docente.FinancieroCaso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** El caso como lo ve el estudiante (mismos nombres que caso.model.ts), sin los Resultados. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoEstudianteResponse {

    private Long id;
    private Long idSimulacion;
    private String nombre;
    private String tipo;
    private String mision;
    private String vision;
    private FinancieroCaso financiero;
    private BigDecimal penalizacionMin;
    private BigDecimal penalizacionMax;
    private LocalDateTime fechaVisualizacion;
    private LocalDateTime fechaInicioPartida;
    private LocalDateTime fechaFinPartida;
    /** true desde fechaInicioPartida: desde ahí se muestran las opciones. */
    private boolean partidaIniciada;
    /** true después de fechaFinPartida. */
    private boolean partidaFinalizada;
    /** Vacía hasta que inicia la partida. Nunca incluye el Resultado. */
    private List<OpcionEstudianteResponse> opciones;
}

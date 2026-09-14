package com.estratego.application.dto.docente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoResponse {

    private Long id;
    private String nombre;
    private Long docenteId;
    private Long liderId;
    private List<Long> estudianteIds;
}
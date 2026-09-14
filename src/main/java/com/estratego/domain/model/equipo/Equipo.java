package com.estratego.domain.model.equipo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {

    private Long id;
    private String nombre;
    private Long docenteId;
    private Long liderId;
    private List<Long> estudianteIds;
}
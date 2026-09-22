package com.estratego.application.dto.docente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CargaMasivaResponse {

    private List<CreadoEstudianteResponse> creados;
    private List<ErrorCargaResponse> errores;
}
package com.estratego.application.dto.docente;

import com.estratego.application.dto.auth.UsuarioResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CargaMasivaResponse {

    private List<UsuarioResponse> creados;
    private List<ErrorCargaResponse> errores;

}

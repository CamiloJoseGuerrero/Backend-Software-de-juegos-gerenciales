package com.estratego.presentation.controller;

import com.estratego.application.dto.docente.CargaMasivaResponse;
import com.estratego.application.usecase.CargaMasivaEstudiantesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/docente/estudiantes")
@RequiredArgsConstructor
public class DocenteController {

    private final CargaMasivaEstudiantesService cargaMasivaEstudiantesService;

    @PostMapping(value = "/carga-masiva", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<CargaMasivaResponse> cargarEstudiantes(
            @RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(cargaMasivaEstudiantesService.procesar(archivo));
    }
}
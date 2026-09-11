package com.estratego.presentation.controller;

import com.estratego.application.dto.auth.UsuarioResponse;
import com.estratego.application.dto.docente.CargaMasivaResponse;
import com.estratego.application.usecase.CargaMasivaEstudiantesService;
import com.estratego.application.usecase.DocenteEstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/docente/estudiantes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCENTE')")
public class DocenteController {

    private final CargaMasivaEstudiantesService cargaMasivaEstudiantesService;
    private final DocenteEstudianteService docenteEstudianteService;

    @PostMapping(value = "/carga-masiva", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CargaMasivaResponse> cargarEstudiantes(
            @RequestPart("archivo") MultipartFile archivo,
            Authentication authentication) {
        return ResponseEntity.ok(
                cargaMasivaEstudiantesService.procesar(archivo, authentication.getName())
        );
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarEstudiantes(Authentication authentication) {
        return ResponseEntity.ok(
                docenteEstudianteService.listarEstudiantesDelDocente(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerEstudiante(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(
                docenteEstudianteService.obtenerEstudianteDelDocente(id, authentication.getName())
        );
    }
}
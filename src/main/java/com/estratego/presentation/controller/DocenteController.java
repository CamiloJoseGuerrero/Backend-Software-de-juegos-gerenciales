package com.estratego.presentation.controller;

import com.estratego.application.dto.docente.CargaMasivaResponse;
import com.estratego.application.dto.docente.CrearEstudianteRequest;
import com.estratego.application.dto.docente.CreadoEstudianteResponse;
import com.estratego.application.dto.docente.EstudianteResponse;
import com.estratego.application.usecase.CargaMasivaEstudiantesService;
import com.estratego.application.usecase.DocenteEstudianteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    /**
     * Alta manual. 201 si se creó la cuenta (se envía la contraseña por correo);
     * 200 si el estudiante ya existía y solo se agregó a la lista del docente.
     */
    @PostMapping
    public ResponseEntity<CreadoEstudianteResponse> crearEstudiante(
            @Valid @RequestBody CrearEstudianteRequest request,
            Authentication authentication) {
        CreadoEstudianteResponse respuesta =
                cargaMasivaEstudiantesService.crearManual(request, authentication.getName());
        HttpStatus estado = respuesta.getContrasenaGenerada() != null ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(estado).body(respuesta);
    }

    /** Solo los estudiantes que cargó el docente autenticado. */
    @GetMapping
    public ResponseEntity<List<EstudianteResponse>> listarEstudiantes(Authentication authentication) {
        return ResponseEntity.ok(
                docenteEstudianteService.listarEstudiantesDelDocente(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstudianteResponse> obtenerEstudiante(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(
                docenteEstudianteService.obtenerEstudiante(id, authentication.getName())
        );
    }
}
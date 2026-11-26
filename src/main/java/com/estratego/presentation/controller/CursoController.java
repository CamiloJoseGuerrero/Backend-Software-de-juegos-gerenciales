package com.estratego.presentation.controller;

import com.estratego.application.dto.docente.CrearCursoRequest;
import com.estratego.application.dto.docente.CursoResponse;
import com.estratego.application.dto.docente.EstudianteResponse;
import com.estratego.application.dto.docente.SimulacionResponse;
import com.estratego.application.usecase.CursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docente/cursos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCENTE')")
public class CursoController {

    private final CursoService cursoService;

    @GetMapping
    public ResponseEntity<List<CursoResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(cursoService.listar(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoResponse> obtener(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(cursoService.obtener(id, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<CursoResponse> crear(
            @Valid @RequestBody CrearCursoRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cursoService.crear(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CrearCursoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(cursoService.actualizar(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        cursoService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/estudiantes")
    public ResponseEntity<List<EstudianteResponse>> listarEstudiantes(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(cursoService.listarEstudiantes(id, authentication.getName()));
    }

    @PostMapping("/{id}/estudiantes/{idEstudiante}")
    public ResponseEntity<Void> agregarEstudiante(
            @PathVariable Long id,
            @PathVariable Long idEstudiante,
            Authentication authentication) {
        cursoService.agregarEstudiante(id, idEstudiante, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/estudiantes/{idEstudiante}")
    public ResponseEntity<Void> quitarEstudiante(
            @PathVariable Long id,
            @PathVariable Long idEstudiante,
            Authentication authentication) {
        cursoService.quitarEstudiante(id, idEstudiante, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/simulaciones")
    public ResponseEntity<List<SimulacionResponse>> listarSimulaciones(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(cursoService.listarSimulaciones(id, authentication.getName()));
    }
}

package com.estratego.presentation.controller;

import com.estratego.application.dto.docente.ActualizarEmpresaRequest;
import com.estratego.application.dto.docente.CrearEmpresaRequest;
import com.estratego.application.dto.docente.EmpresaResponse;
import com.estratego.application.usecase.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docente")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCENTE')")
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping("/simulaciones/{idSimulacion}/empresas")
    public ResponseEntity<EmpresaResponse> crear(
            @PathVariable Long idSimulacion,
            @Valid @RequestBody CrearEmpresaRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empresaService.crear(idSimulacion, request, authentication.getName()));
    }

    @GetMapping("/simulaciones/{idSimulacion}/empresas")
    public ResponseEntity<List<EmpresaResponse>> listarPorSimulacion(
            @PathVariable Long idSimulacion,
            Authentication authentication) {
        return ResponseEntity.ok(
                empresaService.listarPorSimulacion(idSimulacion, authentication.getName()));
    }

    @GetMapping("/empresas/{id}")
    public ResponseEntity<EmpresaResponse> obtener(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(empresaService.obtener(id, authentication.getName()));
    }

    @PutMapping("/empresas/{id}")
    public ResponseEntity<EmpresaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEmpresaRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(empresaService.actualizar(id, request, authentication.getName()));
    }

    @DeleteMapping("/empresas/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        empresaService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
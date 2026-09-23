package com.estratego.presentation.controller;

import com.estratego.application.dto.docente.ActualizarSimulacionRequest;
import com.estratego.application.dto.docente.CrearSimulacionRequest;
import com.estratego.application.dto.docente.SimulacionResponse;
import com.estratego.application.usecase.SimulacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docente/simulaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCENTE')")
public class SimulacionController {

    private final SimulacionService simulacionService;

    @PostMapping
    public ResponseEntity<SimulacionResponse> crear(
            @Valid @RequestBody CrearSimulacionRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(simulacionService.crear(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<SimulacionResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(simulacionService.listar(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimulacionResponse> obtener(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(simulacionService.obtener(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimulacionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarSimulacionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(simulacionService.actualizar(id, request, authentication.getName()));
    }

    @PostMapping("/{id}/programar")
    public ResponseEntity<SimulacionResponse> programar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(simulacionService.programar(id, authentication.getName()));
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<SimulacionResponse> iniciar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(simulacionService.iniciar(id, authentication.getName()));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<SimulacionResponse> finalizar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(simulacionService.finalizar(id, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        simulacionService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
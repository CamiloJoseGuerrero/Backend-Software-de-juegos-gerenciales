package com.estratego.presentation.controller;

import com.estratego.application.dto.docente.ActualizarIntegranteRequest;
import com.estratego.application.dto.docente.AgregarIntegranteRequest;
import com.estratego.application.dto.docente.IntegranteResponse;
import com.estratego.application.usecase.IntegranteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docente/empresas/{idEmpresa}/integrantes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCENTE')")
public class IntegranteController {

    private final IntegranteService integranteService;

    @PostMapping
    public ResponseEntity<IntegranteResponse> agregar(
            @PathVariable Long idEmpresa,
            @Valid @RequestBody AgregarIntegranteRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(integranteService.agregar(idEmpresa, request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<IntegranteResponse>> listar(
            @PathVariable Long idEmpresa,
            Authentication authentication) {
        return ResponseEntity.ok(integranteService.listar(idEmpresa, authentication.getName()));
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<IntegranteResponse> actualizar(
            @PathVariable Long idEmpresa,
            @PathVariable Long idUsuario,
            @Valid @RequestBody ActualizarIntegranteRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                integranteService.actualizar(idEmpresa, idUsuario, request, authentication.getName()));
    }

    @PutMapping("/{idUsuario}/lider")
    public ResponseEntity<IntegranteResponse> asignarLider(
            @PathVariable Long idEmpresa,
            @PathVariable Long idUsuario,
            Authentication authentication) {
        return ResponseEntity.ok(
                integranteService.asignarLider(idEmpresa, idUsuario, authentication.getName()));
    }

    @DeleteMapping("/{idUsuario}")
    public ResponseEntity<Void> quitar(
            @PathVariable Long idEmpresa,
            @PathVariable Long idUsuario,
            Authentication authentication) {
        integranteService.quitar(idEmpresa, idUsuario, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

package com.estratego.presentation.controller;

import com.estratego.application.dto.docente.ActualizarEquipoRequest;
import com.estratego.application.dto.docente.CrearEquipoRequest;
import com.estratego.application.dto.docente.EquipoResponse;
import com.estratego.application.usecase.EquipoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docente/equipos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCENTE')")
public class EquipoController {

    private final EquipoService equipoService;

    @PostMapping
    public ResponseEntity<EquipoResponse> crear(
            @Valid @RequestBody CrearEquipoRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipoService.crear(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<EquipoResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(equipoService.listar(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipoResponse> obtener(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(equipoService.obtener(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEquipoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(equipoService.actualizar(id, request, authentication.getName()));
    }
}
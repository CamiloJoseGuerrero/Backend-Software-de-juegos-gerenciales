package com.estratego.presentation.controller;

import com.estratego.application.dto.docente.ActualizarPartidaRequest;
import com.estratego.application.dto.docente.CrearPartidaRequest;
import com.estratego.application.dto.docente.PartidaResponse;
import com.estratego.application.usecase.PartidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.estratego.application.dto.docente.AsignarEquiposRequest;
import com.estratego.application.dto.docente.PartidaEquiposResponse;

import java.util.List;

@RestController
@RequestMapping("/api/docente/partidas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCENTE')")
public class PartidaController {

    private final PartidaService partidaService;

    @PostMapping
    public ResponseEntity<PartidaResponse> crear(
            @Valid @RequestBody CrearPartidaRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(partidaService.crear(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<PartidaResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(partidaService.listar(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidaResponse> obtener(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(partidaService.obtener(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartidaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPartidaRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(partidaService.actualizar(id, request, authentication.getName()));
    }

    @PostMapping("/{id}/programar")
    public ResponseEntity<PartidaResponse> programar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(partidaService.programar(id, authentication.getName()));
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<PartidaResponse> iniciar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(partidaService.iniciar(id, authentication.getName()));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<PartidaResponse> finalizar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(partidaService.finalizar(id, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        partidaService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

        @PostMapping("/{id}/equipos")
    public ResponseEntity<PartidaEquiposResponse> asignarEquipos(
            @PathVariable Long id,
            @Valid @RequestBody AsignarEquiposRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                partidaService.asignarEquipos(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}/equipos/{equipoId}")
    public ResponseEntity<PartidaEquiposResponse> quitarEquipo(
            @PathVariable Long id,
            @PathVariable Long equipoId,
            Authentication authentication) {
        return ResponseEntity.ok(
                partidaService.quitarEquipo(id, equipoId, authentication.getName()));
    }

    @GetMapping("/{id}/equipos")
    public ResponseEntity<PartidaEquiposResponse> listarEquipos(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(
                partidaService.listarEquipos(id, authentication.getName()));
    }
}
package com.estratego.presentation.controller;

import com.estratego.application.dto.docente.CasoRequest;
import com.estratego.application.dto.docente.CasoResponse;
import com.estratego.application.dto.docente.DecisionEmpresaResponse;
import com.estratego.application.usecase.CasoService;
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
public class CasoController {

    private final CasoService casoService;

    /** Igual que el front: la simulación va en el body (idSimulacion). */
    @PostMapping("/casos")
    public ResponseEntity<CasoResponse> crear(
            @Valid @RequestBody CasoRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(casoService.crear(request.getIdSimulacion(), request, authentication.getName()));
    }

    @PostMapping("/simulaciones/{idSimulacion}/casos")
    public ResponseEntity<CasoResponse> crearEnSimulacion(
            @PathVariable Long idSimulacion,
            @Valid @RequestBody CasoRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(casoService.crear(idSimulacion, request, authentication.getName()));
    }

    @GetMapping("/casos")
    public ResponseEntity<List<CasoResponse>> listarDelDocente(Authentication authentication) {
        return ResponseEntity.ok(casoService.listarDelDocente(authentication.getName()));
    }

    @GetMapping("/simulaciones/{idSimulacion}/casos")
    public ResponseEntity<List<CasoResponse>> listarPorSimulacion(
            @PathVariable Long idSimulacion,
            Authentication authentication) {
        return ResponseEntity.ok(casoService.listarPorSimulacion(idSimulacion, authentication.getName()));
    }

    @GetMapping("/casos/{id}")
    public ResponseEntity<CasoResponse> obtener(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(casoService.obtener(id, authentication.getName()));
    }

    @PutMapping("/casos/{id}")
    public ResponseEntity<CasoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CasoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(casoService.actualizar(id, request, authentication.getName()));
    }

    @PostMapping("/casos/{id}/activar")
    public ResponseEntity<CasoResponse> activar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(casoService.activar(id, authentication.getName()));
    }

    @GetMapping("/casos/{id}/decisiones")
    public ResponseEntity<List<DecisionEmpresaResponse>> decisiones(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(casoService.decisiones(id, authentication.getName()));
    }

    @DeleteMapping("/casos/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        casoService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

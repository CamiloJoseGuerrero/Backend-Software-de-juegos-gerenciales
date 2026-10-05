package com.estratego.presentation.controller;

import com.estratego.application.dto.clasificacion.ClasificacionResponse;
import com.estratego.application.dto.estudiante.CasoActualResponse;
import com.estratego.application.dto.estudiante.CasoEstudianteResponse;
import com.estratego.application.dto.estudiante.DecisionRequest;
import com.estratego.application.dto.estudiante.DecisionResponse;
import com.estratego.application.dto.estudiante.MiEmpresaResponse;
import com.estratego.application.dto.estudiante.MiSimulacionResponse;
import com.estratego.application.usecase.ClasificacionService;
import com.estratego.application.usecase.PortalEstudianteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudiante")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ESTUDIANTE')")
public class EstudianteController {

    private final PortalEstudianteService portalEstudianteService;
    private final ClasificacionService clasificacionService;

    /** Ranking final: solo con la simulación FINALIZADA (si no, 400). */
    @GetMapping("/simulaciones/{idSimulacion}/clasificacion")
    public ResponseEntity<ClasificacionResponse> clasificacion(
            @PathVariable Long idSimulacion,
            Authentication authentication) {
        return ResponseEntity.ok(clasificacionService.paraEstudiante(idSimulacion, authentication.getName()));
    }

    @GetMapping("/simulaciones")
    public ResponseEntity<List<MiSimulacionResponse>> misSimulaciones(Authentication authentication) {
        return ResponseEntity.ok(portalEstudianteService.misSimulaciones(authentication.getName()));
    }

    @GetMapping("/empresas/{idEmpresa}")
    public ResponseEntity<MiEmpresaResponse> miEmpresa(
            @PathVariable Long idEmpresa,
            Authentication authentication) {
        return ResponseEntity.ok(portalEstudianteService.miEmpresa(idEmpresa, authentication.getName()));
    }

    @GetMapping("/simulaciones/{idSimulacion}/casos")
    public ResponseEntity<List<CasoEstudianteResponse>> casos(
            @PathVariable Long idSimulacion,
            Authentication authentication) {
        return ResponseEntity.ok(portalEstudianteService.casosDeSimulacion(idSimulacion, authentication.getName()));
    }

    @GetMapping("/casos/{id}")
    public ResponseEntity<CasoEstudianteResponse> caso(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(portalEstudianteService.caso(id, authentication.getName()));
    }

    /** 200 con el caso activo, o 204 (sin cuerpo) si el estudiante no tiene caso activo visible. */
    @GetMapping("/caso-actual")
    public ResponseEntity<CasoActualResponse> casoActual(
            @RequestParam(required = false) Long idSimulacion,
            Authentication authentication) {
        return portalEstudianteService.casoActual(authentication.getName(), idSimulacion)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/decision")
    public ResponseEntity<DecisionResponse> decidir(
            @Valid @RequestBody DecisionRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portalEstudianteService.decidir(request, authentication.getName()));
    }
}

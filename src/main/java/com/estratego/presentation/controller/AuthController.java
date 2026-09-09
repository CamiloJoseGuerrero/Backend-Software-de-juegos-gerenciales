package com.estratego.presentation.controller;

import com.estratego.application.dto.auth.LoginRequest;
import com.estratego.application.dto.auth.LoginResponse;
import com.estratego.application.dto.auth.RegistroDocenteRequest;
import com.estratego.application.dto.auth.SesionResponse;
import com.estratego.application.usecase.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro-docente")
    public ResponseEntity<LoginResponse> registroDocente(@Valid @RequestBody RegistroDocenteRequest request) {
        LoginResponse response = authService.registroDocente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sesion")
    public ResponseEntity<SesionResponse> obtenerSesion(Authentication authentication) {
        SesionResponse response = authService.obtenerSesion(authentication.getName());
        return ResponseEntity.ok(response);
    }

}

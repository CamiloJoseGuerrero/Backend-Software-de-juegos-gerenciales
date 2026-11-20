package com.estratego.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Para el health check de Render y el "despertador" que evita que el servicio se duerma.
 * Público y sin tocar la base: así no mantiene despierta la base de Neon (que se suspende a los 5 min).
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}

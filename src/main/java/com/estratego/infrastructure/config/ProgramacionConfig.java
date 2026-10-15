package com.estratego.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/** Tareas programadas (cierre automático de casos) y el reloj de la aplicación (zona por defecto: Bogotá). */
@Configuration
@EnableScheduling
public class ProgramacionConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

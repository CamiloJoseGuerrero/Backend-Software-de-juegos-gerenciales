package com.estratego.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.ZoneId;

/** Las fechas-hora que llegan con "Z" o con desfase se convierten a la zona de la aplicación. */
@Configuration
public class FechasConfig {

    @Value("${app.zona-horaria:America/Bogota}")
    private String zonaHoraria;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer fechasEnHoraLocal() {
        ZoneId zona = ZoneId.of(zonaHoraria);
        return builder -> builder.deserializerByType(LocalDateTime.class, new FechaLocalFlexibleDeserializer(zona));
    }
}

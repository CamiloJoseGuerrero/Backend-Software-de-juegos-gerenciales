package com.estratego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
public class EstrategoApplication {

    public static void main(String[] args) {
        String zona = System.getenv().getOrDefault("APP_ZONA_HORARIA", "America/Bogota");
        TimeZone.setDefault(TimeZone.getTimeZone(zona));
        SpringApplication.run(EstrategoApplication.class, args);
    }

}

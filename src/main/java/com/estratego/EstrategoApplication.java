package com.estratego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EstrategoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstrategoApplication.class, args);
    }

}

package com.unapi.rotaract.rotaract_d4465_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

     @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
            .info(new Info()
                .title("Plataforma Distrital de Voluntariado Rotaract D4465")
                .description("API REST para gestión de voluntariado, clubes y usuarios.")
                .version("1.0.0"));
    }

}

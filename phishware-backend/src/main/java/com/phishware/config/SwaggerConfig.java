package com.phishware.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI phishwareOpenAPI() {
        SecurityScheme jwtScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .name("JWT Authentication");

        return new OpenAPI()
            .info(new Info()
                .title("PHISHWARE API")
                .description("API REST para el sistema de detección y prevención de ataques de phishing. " +
                             "Incluye análisis de URLs, alertas preventivas y módulo educativo.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("PHISHWARE Team")
                    .email("dev@phishware.com"))
                .license(new License()
                    .name("MIT License")))
            .addSecurityItem(new SecurityRequirement().addList("JWT Authentication"))
            .components(new Components()
                .addSecuritySchemes("JWT Authentication", jwtScheme));
    }
}

package com.phishware.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        // La extensión (popup y service worker) envía Origin: chrome-extension://<id>, moz-extension://...
        // El id cambia por instalación, así que se admite por patrón, pero SOLO en endpoints
        // públicos de solo lectura y sin credenciales: no expone sesiones ni datos de usuario.
        CorsConfiguration extension = new CorsConfiguration();
        extension.setAllowedOriginPatterns(new java.util.ArrayList<>(List.of(
            "chrome-extension://*", "moz-extension://*", "safari-web-extension://*")));
        allowedOrigins.forEach(extension::addAllowedOriginPattern);
        extension.setAllowedMethods(List.of("GET", "OPTIONS"));
        extension.setAllowedHeaders(List.of("Content-Type"));
        extension.setAllowCredentials(false);
        extension.setMaxAge(3600L);

        // El primer patrón que coincide gana: los específicos van antes de "/**"
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/public/url/**", extension);
        source.registerCorsConfiguration("/actuator/health", extension);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

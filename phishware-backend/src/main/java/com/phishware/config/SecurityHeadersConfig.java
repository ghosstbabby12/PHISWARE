package com.phishware.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * OWASP A05 - Security Misconfiguration
 * Agrega headers HTTP de seguridad en todas las respuestas.
 *
 * Referencias:
 *  - OWASP Secure Headers Project
 *  - NIST SP 800-53 SI-10 (Information Input Validation)
 *  - NIST CSF PR.DS-2 (Data-in-transit is protected)
 */
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public SecurityHeadersFilter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }

    public static class SecurityHeadersFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain chain) throws ServletException, IOException {

            // OWASP: Evitar que el navegador infiera el tipo de contenido
            response.setHeader("X-Content-Type-Options", "nosniff");

            // OWASP A07: Prevención de clickjacking
            response.setHeader("X-Frame-Options", "DENY");

            // OWASP A03: Protección XSS legacy (IE/Edge antiguo)
            response.setHeader("X-XSS-Protection", "1; mode=block");

            // NIST CSF PR.DS-2: Datos en tránsito protegidos (HSTS)
            response.setHeader(
                "Strict-Transport-Security",
                "max-age=31536000; includeSubDomains; preload"
            );

            // OWASP A05: Content Security Policy — previene XSS moderno
            response.setHeader(
                "Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com; " +
                "img-src 'self' data: https:; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none'; " +
                "form-action 'self';"
            );

            // OWASP: No exponer información del servidor
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // NIST CSF DE.CM-7: Monitoreo de actividad no autorizada
            response.setHeader("Permissions-Policy",
                "camera=(), microphone=(), geolocation=(), payment=()"
            );

            // No cachear respuestas de la API (datos sensibles)
            if (request.getRequestURI().contains("/api/")) {
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                response.setHeader("Pragma", "no-cache");
            }

            // Ocultar información del servidor (OWASP A05)
            response.setHeader("Server", "PHISHWARE");

            chain.doFilter(request, response);
        }
    }
}

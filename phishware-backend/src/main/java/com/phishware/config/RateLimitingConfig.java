package com.phishware.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OWASP A04:2021 - Insecure Design
 * OWASP A09:2021 - Security Logging and Monitoring Failures
 * NIST CSF DE.CM-7: Monitoring for unauthorized personnel, connections, devices, and software
 * NIST SP 800-53 SC-5: Denial-of-Service Protection
 *
 * Rate limiting simple por IP para proteger los endpoints críticos.
 * En producción se recomienda reemplazar por Redis + Bucket4J.
 */
@Configuration
@Slf4j
public class RateLimitingConfig {

    @Value("${app.rate-limit.requests-per-minute:30}")
    private int requestsPerMinute;

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter(requestsPerMinute);
    }

    @Slf4j
    public static class RateLimitFilter extends OncePerRequestFilter {

        private final int maxRequestsPerMinute;

        // ip -> [count, windowStartEpochSecond]
        private final Map<String, long[]> requestCounts = new ConcurrentHashMap<>();

        // Endpoints que requieren rate limiting estricto
        private static final java.util.Set<String> RATE_LIMITED_PATHS = java.util.Set.of(
            "/auth/login",
            "/auth/register",
            "/analysis"
        );

        public RateLimitFilter(int maxRequestsPerMinute) {
            this.maxRequestsPerMinute = maxRequestsPerMinute;
        }

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain chain) throws ServletException, IOException {

            String path = request.getRequestURI().replaceFirst("^/api", "");

            boolean requiresLimit = RATE_LIMITED_PATHS.stream()
                .anyMatch(p -> path.startsWith(p));

            if (!requiresLimit) {
                chain.doFilter(request, response);
                return;
            }

            String clientIp = extractIp(request);
            long nowSeconds = Instant.now().getEpochSecond();

            long[] state = requestCounts.compute(clientIp, (ip, existing) -> {
                if (existing == null || nowSeconds - existing[1] >= 60) {
                    return new long[]{1, nowSeconds};
                }
                existing[0]++;
                return existing;
            });

            long count = state[0];

            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxRequestsPerMinute - count)));

            if (count > maxRequestsPerMinute) {
                // OWASP A09: Registrar intentos excesivos
                log.warn("[SECURITY] Rate limit exceeded — IP: {} Path: {} Count: {}", clientIp, path, count);

                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\"," +
                    "\"message\":\"Has excedido el límite de solicitudes. Intenta en 60 segundos.\"}"
                );
                return;
            }

            chain.doFilter(request, response);
        }

        private String extractIp(HttpServletRequest request) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].strip();
            }
            return request.getRemoteAddr();
        }
    }
}

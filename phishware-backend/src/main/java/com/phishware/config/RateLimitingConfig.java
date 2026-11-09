package com.phishware.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
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

    @Value("${app.rate-limit.requests-per-minute:120}")
    private int requestsPerMinute;

    @Value("#{'${app.security.trusted-proxies:}'.split(',')}")
    private java.util.List<String> trustedProxies;

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter(requestsPerMinute, trustedProxies);
    }

    @Slf4j
    public static class RateLimitFilter extends OncePerRequestFilter {

        private final int maxRequestsPerMinute;
        private final java.util.List<String> trustedProxies;

        // ip -> [count, windowStartEpochSecond]
        private final Map<String, long[]> requestCounts = new ConcurrentHashMap<>();

        // Endpoints que requieren rate limiting estricto
        private static final java.util.Set<String> RATE_LIMITED_PATHS = java.util.Set.of(
            "/auth/login",
            "/auth/register",
            "/analysis",
            "/public/url",
            "/security/password-check"
        );

        public RateLimitFilter(int maxRequestsPerMinute, java.util.List<String> trustedProxies) {
            this.maxRequestsPerMinute = maxRequestsPerMinute;
            this.trustedProxies = trustedProxies == null ? java.util.List.of() : trustedProxies;
        }

        @Override
        protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

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
            String remoteAddr = request.getRemoteAddr();
            if (isTrustedProxy(remoteAddr)) {
                String forwarded = request.getHeader("X-Forwarded-For");
                if (forwarded != null && !forwarded.isBlank()) {
                    return forwarded.split(",")[0].strip();
                }
            }
            return remoteAddr;
        }

        private boolean isTrustedProxy(String remoteAddr) {
            for (String entry : trustedProxies) {
                String candidate = entry.strip();
                if (candidate.isEmpty()) {
                    continue;
                }
                if (candidate.equals(remoteAddr)) {
                    return true;
                }
                if (candidate.contains("/") && matchesCidr(candidate, remoteAddr)) {
                    return true;
                }
            }
            return false;
        }

        static boolean matchesCidr(String cidr, String ip) {
            try {
                String[] parts = cidr.split("/");
                int prefix = Integer.parseInt(parts[1]);
                long network = toLong(java.net.InetAddress.getByName(parts[0]));
                long address = toLong(java.net.InetAddress.getByName(ip));
                long mask = prefix == 0 ? 0L : (~0L << (32 - prefix)) & 0xFFFFFFFFL;
                return (network & mask) == (address & mask);
            } catch (Exception e) {
                return false;
            }
        }

        private static long toLong(java.net.InetAddress address) {
            byte[] raw = address.getAddress();
            if (raw.length != 4) {
                return -1;
            }
            return ((raw[0] & 0xFFL) << 24) | ((raw[1] & 0xFFL) << 16)
                | ((raw[2] & 0xFFL) << 8) | (raw[3] & 0xFFL);
        }
    }
}

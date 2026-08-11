package com.phishware.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * OWASP Top 10:2021 — Validador de Cumplimiento
 *
 * Centraliza las validaciones alineadas con el OWASP Top 10.
 * Cada método documenta el control OWASP que implementa.
 *
 * A01 - Broken Access Control
 * A02 - Cryptographic Failures
 * A03 - Injection
 * A04 - Insecure Design
 * A05 - Security Misconfiguration
 * A06 - Vulnerable and Outdated Components
 * A07 - Identification and Authentication Failures
 * A08 - Software and Data Integrity Failures
 * A09 - Security Logging and Monitoring Failures
 * A10 - Server-Side Request Forgery (SSRF)
 */
@Component
@Slf4j
public class OwaspComplianceValidator {

    public OwaspComplianceValidator() {
    }

    // ── A01: Broken Access Control ────────────────────────────────────────────

    /**
     * Verifica que un usuario solo acceda a sus propios recursos.
     * Previene IDOR (Insecure Direct Object Reference).
     */
    public void validateOwnership(Long resourceOwnerId, Long requestingUserId, String resourceType) {
        if (!resourceOwnerId.equals(requestingUserId)) {
            log.warn("[OWASP A01] Intento de acceso no autorizado — recurso: {} owner: {} requestor: {}",
                resourceType, resourceOwnerId, requestingUserId);
            throw new SecurityException(
                "Acceso denegado: no tienes permiso para acceder a este recurso"
            );
        }
    }

    // ── A03: Injection Prevention ─────────────────────────────────────────────

    /**
     * Valida que la URL no contenga payloads de inyección.
     * Protege contra: SQL Injection, LDAP Injection, Command Injection via URL.
     */
    public ValidationResult validateUrlForInjection(String url) {
        List<String> violations = new ArrayList<>();

        if (url == null || url.isBlank()) {
            violations.add("URL vacía");
            return new ValidationResult(false, violations);
        }

        // SQL Injection patterns en URL
        String urlUpper = url.toUpperCase();
        List<String> sqlPatterns = List.of(
            "' OR ", "\" OR ", "1=1", "1 = 1",
            "DROP TABLE", "DELETE FROM", "INSERT INTO",
            "UNION SELECT", "--", "/*", "*/"
        );
        for (String pattern : sqlPatterns) {
            if (urlUpper.contains(pattern.toUpperCase())) {
                violations.add("Posible SQL Injection detectado: " + pattern);
                log.warn("[OWASP A03] SQL Injection pattern en URL: {}", pattern);
            }
        }

        // Command Injection
        List<String> cmdPatterns = List.of("; ls", "| cat", "& dir", "$(", "`");
        for (String pattern : cmdPatterns) {
            if (url.contains(pattern)) {
                violations.add("Posible Command Injection: " + pattern);
                log.warn("[OWASP A03] Command Injection pattern en URL: {}", pattern);
            }
        }

        return new ValidationResult(violations.isEmpty(), violations);
    }

    // ── A07: Authentication Failures ─────────────────────────────────────────

    /**
     * Valida la fortaleza de una contraseña nueva.
     * NIST SP 800-63B: Digital Identity Guidelines — Passwords.
     */
    public ValidationResult validatePasswordStrength(String password) {
        List<String> violations = new ArrayList<>();

        if (password == null) {
            violations.add("La contraseña es requerida");
            return new ValidationResult(false, violations);
        }

        if (password.length() < 8) violations.add("Mínimo 8 caracteres (NIST SP 800-63B)");
        if (password.length() > 128) violations.add("Máximo 128 caracteres");
        if (!password.matches(".*[A-Z].*")) violations.add("Requiere al menos una letra mayúscula");
        if (!password.matches(".*[a-z].*")) violations.add("Requiere al menos una letra minúscula");
        if (!password.matches(".*\\d.*")) violations.add("Requiere al menos un número");
        if (!password.matches(".*[@$!%*?&_\\-#].*")) violations.add("Requiere al menos un carácter especial");

        // NIST: Rechazar contraseñas comunes
        List<String> commonPasswords = List.of(
            "password", "123456", "password123", "admin123",
            "qwerty", "letmein", "welcome"
        );
        if (commonPasswords.contains(password.toLowerCase())) {
            violations.add("La contraseña es demasiado común (NIST SP 800-63B)");
        }

        return new ValidationResult(violations.isEmpty(), violations);
    }

    // ── A10: SSRF Prevention ─────────────────────────────────────────────────

    /**
     * OWASP A10:2021 - Server-Side Request Forgery
     * Previene que un atacante use el servidor para escanear la red interna
     * mediante URLs que apuntan a recursos internos.
     */
    public void validateAgainstSsrf(String url, String domain) {
        if (domain == null) return;

        String domainLower = domain.toLowerCase();

        // Bloquear rangos de IP privados (RFC 1918)
        List<String> internalPatterns = List.of(
            "localhost", "127.", "10.", "172.16.", "172.17.", "172.18.",
            "172.19.", "172.20.", "172.21.", "172.22.", "172.23.",
            "172.24.", "172.25.", "172.26.", "172.27.", "172.28.",
            "172.29.", "172.30.", "172.31.", "192.168.",
            "169.254.", "::1", "0.0.0.0",
            // Cloud metadata endpoints (AWS, GCP, Azure)
            "169.254.169.254", "metadata.google.internal"
        );

        for (String pattern : internalPatterns) {
            if (domainLower.contains(pattern)) {
                log.warn("[OWASP A10] SSRF attempt detected — domain: {}", domain);
                throw new SecurityException(
                    "Política de seguridad: no se permiten URLs que apunten a recursos internos de red"
                );
            }
        }
    }

    // ── A09: Security Logging ─────────────────────────────────────────────────

    /**
     * Registra eventos de seguridad relevantes.
     * OWASP A09 + NIST CSF DE.CM-3: Personnel activity is monitored.
     */
    public void logSecurityEvent(String eventType, String detail, String ip) {
        log.warn("[SECURITY EVENT] type={} detail='{}' ip={}", eventType, detail, ip);
    }

    // ── DTO de resultado ──────────────────────────────────────────────────────

    public record ValidationResult(boolean valid, List<String> violations) {
        public String firstViolation() {
            return violations.isEmpty() ? null : violations.get(0);
        }
    }
}

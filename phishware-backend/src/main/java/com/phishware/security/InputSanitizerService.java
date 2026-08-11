package com.phishware.security;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * OWASP A03:2021 - Injection Prevention
 * OWASP A01:2021 - Broken Access Control (validación de entradas)
 * NIST SP 800-53 SI-10: Information Input Validation
 *
 * Centraliza la sanitización y validación de entradas del usuario.
 * Toda entrada externa pasa por aquí antes de procesarse.
 */
@Service
public class InputSanitizerService {

    // Patrones de caracteres peligrosos para XSS (OWASP A07)
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<[^>]*>|javascript:|vbscript:|on\\w+\\s*=|<script|</script|<iframe|<object|<embed",
        Pattern.CASE_INSENSITIVE
    );

    // Esquemas de URL permitidos (OWASP: validación de esquemas)
    private static final List<String> ALLOWED_SCHEMES = List.of("http", "https");

    // Patrones de redirección abierta peligrosa (OWASP A01)
    private static final List<String> DANGEROUS_PATTERNS = List.of(
        "javascript:", "data:", "vbscript:", "file://",
        "\\x00", "%00", "../", "..\\",
        "<script", "onclick=", "onerror=", "onload="
    );

    /**
     * Sanitiza texto libre eliminando caracteres HTML peligrosos.
     * Protege contra XSS (OWASP A03, A07).
     */
    public String sanitizeText(String input) {
        if (input == null) return null;

        String sanitized = input.trim();

        // Prevenir XSS: escapar caracteres HTML
        sanitized = sanitized
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");

        return sanitized;
    }

    /**
     * Valida y sanitiza una URL de entrada.
     * OWASP A03: Previene inyección mediante URLs malformadas.
     * NIST SI-10: Valida integridad de la entrada.
     *
     * @return URL sanitizada o lanza excepción si es inválida/peligrosa
     */
    public String sanitizeAndValidateUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("La URL no puede estar vacía");
        }

        // Limitar longitud (OWASP: prevenir buffer overflow / DoS)
        if (rawUrl.length() > 2048) {
            throw new IllegalArgumentException("La URL excede el límite de 2048 caracteres");
        }

        String url = rawUrl.strip();

        // Detección de patrones peligrosos (OWASP A03 - Injection)
        String urlLower = url.toLowerCase();
        for (String dangerous : DANGEROUS_PATTERNS) {
            if (urlLower.contains(dangerous.toLowerCase())) {
                throw new SecurityException(
                    "La URL contiene patrones no permitidos por política de seguridad"
                );
            }
        }

        // Verificar si contiene XSS embebido
        if (XSS_PATTERN.matcher(url).find()) {
            throw new SecurityException("La URL contiene contenido potencialmente malicioso");
        }

        // Agregar esquema si falta
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        // Parsear y validar estructura
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Formato de URL inválido: " + rawUrl);
        }

        // Validar esquema (OWASP: solo http/https permitidos)
        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new SecurityException(
                "Solo se permiten URLs con esquemas http:// o https://"
            );
        }

        // Validar que tenga host
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("La URL no contiene un dominio válido");
        }

        return url;
    }

    /**
     * Verifica si un nombre de usuario tiene formato válido.
     * OWASP A07: Previene enumeración y ataques de fuerza bruta.
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.length() < 3 || username.length() > 50) return false;
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Verifica si un email tiene formato válido.
     * OWASP A03: Previene inyección de headers mediante email.
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.length() > 150) return false;
        return email.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Verifica fortaleza de contraseña.
     * NIST SP 800-63B: Password Strength Requirements.
     * Mínimo 8 chars, mayúscula, minúscula, número, símbolo.
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-#])[A-Za-z\\d@$!%*?&_\\-#]{8,}$");
    }

    /**
     * Extrae el dominio de una URL de forma segura.
     */
    public String extractDomainSafely(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            return sanitizeText(url);
        }
    }
}

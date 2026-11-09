package com.phishware.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Password breach check with k-anonymity (Troy Hunt / HaveIBeenPwned model).
 *
 * The server only ever receives the first 5 hex characters of the SHA-1 hash,
 * queries the public HIBP range API and returns the matching suffixes. The full
 * password hash is completed and compared on the client, so the password never
 * leaves the user's device and the server never sees it either.
 */
@Service
@Slf4j
public class PasswordBreachService {

    private final WebClient webClient;

    public PasswordBreachService(@Qualifier("hibpClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public record RangeResult(String prefix, Map<String, Integer> suffixes, boolean serviceAvailable) {}

    public RangeResult checkPrefix(String rawPrefix) {
        String prefix = normalizePrefix(rawPrefix);
        String body;
        try {
            body = webClient.get()
                .uri("/range/{prefix}", prefix)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Exception e) {
            log.warn("HIBP range API no disponible: {}", e.getMessage());
            return new RangeResult(prefix, Map.of(), false);
        }
        return new RangeResult(prefix, parseRangeBody(body), true);
    }

    static String normalizePrefix(String rawPrefix) {
        if (rawPrefix == null) {
            throw new IllegalArgumentException("El prefijo es requerido");
        }
        String prefix = rawPrefix.trim().toUpperCase();
        if (!prefix.matches("[0-9A-F]{5}")) {
            throw new IllegalArgumentException("El prefijo debe ser exactamente 5 caracteres hexadecimales");
        }
        return prefix;
    }

    static Map<String, Integer> parseRangeBody(String body) {
        Map<String, Integer> suffixes = new LinkedHashMap<>();
        if (body == null || body.isBlank()) {
            return suffixes;
        }
        for (String line : body.split("\r?\n")) {
            String[] parts = line.split(":");
            if (parts.length == 2 && parts[0].trim().matches("[0-9A-F]{35}")) {
                try {
                    suffixes.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException e) {
                    log.debug("Línea HIBP ignorada: {}", line);
                }
            }
        }
        return suffixes;
    }
}

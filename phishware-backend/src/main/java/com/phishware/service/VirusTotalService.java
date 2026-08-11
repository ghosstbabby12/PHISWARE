package com.phishware.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class VirusTotalService {

    private final WebClient webClient;

    public VirusTotalService(@Qualifier("virusTotalClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public VirusTotalResult analyzeUrl(String url) {
        try {
            // Encode URL to base64 (VirusTotal v3 requirement)
            String urlId = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(url.getBytes());

            Map<String, Object> response = webClient.get()
                .uri("/urls/{id}", urlId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    // URL not in cache, submit for analysis
                    return submitAndWait(url);
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Error VirusTotal API: {} - {}", ex.getStatusCode(), ex.getMessage());
                    return Mono.empty();
                })
                .block();

            return parseResponse(response);

        } catch (Exception e) {
            log.error("Error al consultar VirusTotal: {}", e.getMessage());
            return VirusTotalResult.error();
        }
    }

    private Mono<Map<String, Object>> submitAndWait(String url) {
        return webClient.post()
            .uri("/urls")
            .bodyValue("url=" + url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .retrieve()
            .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
            .onErrorResume(e -> {
                log.warn("No se pudo enviar URL a VirusTotal: {}", e.getMessage());
                return Mono.empty();
            });
    }

    @SuppressWarnings("unchecked")
    private VirusTotalResult parseResponse(Map<String, Object> response) {
        if (response == null) {
            return VirusTotalResult.error();
        }

        try {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null) return VirusTotalResult.safe(0, 0);

            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
            if (attributes == null) return VirusTotalResult.safe(0, 0);

            Map<String, Object> lastAnalysisStats = (Map<String, Object>) attributes.get("last_analysis_stats");
            if (lastAnalysisStats == null) return VirusTotalResult.safe(0, 0);

            int malicious = ((Number) lastAnalysisStats.getOrDefault("malicious", 0)).intValue();
            int suspicious = ((Number) lastAnalysisStats.getOrDefault("suspicious", 0)).intValue();
            int total = ((Number) lastAnalysisStats.getOrDefault("harmless", 0)).intValue()
                      + malicious + suspicious
                      + ((Number) lastAnalysisStats.getOrDefault("undetected", 0)).intValue();

            if (malicious > 0) {
                return VirusTotalResult.malicious(malicious, total);
            } else if (suspicious > 0) {
                return VirusTotalResult.suspicious(suspicious, total);
            } else {
                return VirusTotalResult.safe(0, total);
            }

        } catch (ClassCastException e) {
            log.error("Error parsing VirusTotal response: {}", e.getMessage());
            return VirusTotalResult.error();
        }
    }

    public record VirusTotalResult(
        boolean isError,
        int maliciousCount,
        int suspiciousCount,
        int totalEngines,
        ThreatLevel threatLevel
    ) {
        enum ThreatLevel { SAFE, SUSPICIOUS, MALICIOUS, UNKNOWN }

        static VirusTotalResult safe(int total, int engines) {
            return new VirusTotalResult(false, 0, 0, engines, ThreatLevel.SAFE);
        }

        static VirusTotalResult suspicious(int suspicious, int engines) {
            return new VirusTotalResult(false, 0, suspicious, engines, ThreatLevel.SUSPICIOUS);
        }

        static VirusTotalResult malicious(int malicious, int engines) {
            return new VirusTotalResult(false, malicious, 0, engines, ThreatLevel.MALICIOUS);
        }

        static VirusTotalResult error() {
            return new VirusTotalResult(true, 0, 0, 0, ThreatLevel.UNKNOWN);
        }

        public double maliciousPercentage() {
            if (totalEngines == 0) return 0.0;
            return (double) maliciousCount / totalEngines * 100;
        }
    }
}

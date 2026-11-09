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

    @org.springframework.beans.factory.annotation.Value("${app.virus-total.api-key}")
    private String apiKey;

    @org.springframework.beans.factory.annotation.Value("${app.virus-total.poll-attempts:5}")
    private int pollAttempts;

    @org.springframework.beans.factory.annotation.Value("${app.virus-total.poll-interval-ms:3000}")
    private long pollIntervalMs;

    public VirusTotalService(@Qualifier("virusTotalClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("YOUR_API_KEY");
    }

    public VirusTotalResult analyzeUrl(String url) {
        if (!isConfigured()) {
            log.debug("VirusTotal no configurado — omitiendo llamada API");
            return VirusTotalResult.error();
        }
        try {
            // Encode URL to base64 (VirusTotal v3 requirement)
            String urlId = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(url.getBytes());

            VirusTotalResult result = webClient.get()
                .uri("/urls/{id}", urlId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(cached -> Mono.just(parseResponse(cached)))
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    // URL not in cache: submit it and poll until the verdict is ready
                    return submitAndWait(url);
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Error VirusTotal API: {} - {}", ex.getStatusCode(), ex.getMessage());
                    return Mono.just(VirusTotalResult.error());
                })
                .block();

            return result == null ? VirusTotalResult.error() : result;

        } catch (Exception e) {
            log.error("Error al consultar VirusTotal: {}", e.getMessage());
            return VirusTotalResult.error();
        }
    }

    private Mono<VirusTotalResult> submitAndWait(String url) {
        return webClient.post()
            .uri("/urls")
            .bodyValue("url=" + url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .retrieve()
            .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
            .flatMap(submitResponse -> {
                String analysisId = extractAnalysisId(submitResponse);
                if (analysisId == null) {
                    log.warn("VirusTotal no devolvió analysis id para {}", url);
                    return Mono.<Map<String, Object>>empty();
                }
                return pollAnalysis(analysisId, pollAttempts);
            })
            .map(VirusTotalService::fromAnalysisStats)
            .onErrorResume(e -> {
                log.warn("No se pudo enviar URL a VirusTotal: {}", e.getMessage());
                return Mono.just(VirusTotalResult.error());
            });
    }

    private Mono<Map<String, Object>> pollAnalysis(String analysisId, int remaining) {
        return webClient.get()
            .uri("/analyses/{id}", analysisId)
            .retrieve()
            .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
            .flatMap(analysis -> {
                if (isAnalysisCompleted(analysis) || remaining <= 1) {
                    return Mono.just(analysis);
                }
                return Mono.delay(java.time.Duration.ofMillis(pollIntervalMs))
                    .flatMap(tick -> pollAnalysis(analysisId, remaining - 1));
            })
            .onErrorResume(e -> {
                log.warn("Error consultando análisis {} de VirusTotal: {}", analysisId, e.getMessage());
                return Mono.empty();
            });
    }

    @SuppressWarnings("unchecked")
    static String extractAnalysisId(Map<String, Object> submitResponse) {
        if (submitResponse == null) {
            return null;
        }
        Object data = submitResponse.get("data");
        if (!(data instanceof Map)) {
            return null;
        }
        Object id = ((Map<String, Object>) data).get("id");
        return id instanceof String ? (String) id : null;
    }

    @SuppressWarnings("unchecked")
    static boolean isAnalysisCompleted(Map<String, Object> analysisResponse) {
        if (analysisResponse == null) {
            return false;
        }
        Object data = analysisResponse.get("data");
        if (!(data instanceof Map)) {
            return false;
        }
        Object attributes = ((Map<String, Object>) data).get("attributes");
        if (!(attributes instanceof Map)) {
            return false;
        }
        return "completed".equals(((Map<String, Object>) attributes).get("status"));
    }

    @SuppressWarnings("unchecked")
    static VirusTotalResult fromAnalysisStats(Map<String, Object> analysisResponse) {
        if (analysisResponse == null) {
            return VirusTotalResult.error();
        }
        Object data = analysisResponse.get("data");
        if (!(data instanceof Map)) {
            return VirusTotalResult.error();
        }
        Object attributes = ((Map<String, Object>) data).get("attributes");
        if (!(attributes instanceof Map)) {
            return VirusTotalResult.error();
        }
        Object stats = ((Map<String, Object>) attributes).get("stats");
        if (!(stats instanceof Map)) {
            return VirusTotalResult.error();
        }
        Map<String, Object> counts = (Map<String, Object>) stats;
        int malicious = ((Number) counts.getOrDefault("malicious", 0)).intValue();
        int suspicious = ((Number) counts.getOrDefault("suspicious", 0)).intValue();
        int total = ((Number) counts.getOrDefault("harmless", 0)).intValue()
            + malicious + suspicious
            + ((Number) counts.getOrDefault("undetected", 0)).intValue();
        if (malicious > 0) {
            return VirusTotalResult.malicious(malicious, total);
        } else if (suspicious > 0) {
            return VirusTotalResult.suspicious(suspicious, total);
        } else if (total > 0) {
            return VirusTotalResult.safe(0, total);
        }
        return VirusTotalResult.error();
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

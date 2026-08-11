package com.phishware.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@Slf4j
public class GoogleSafeBrowsingService {

    private final WebClient webClient;

    @Value("${app.google-safe-browsing.api-key}")
    private String apiKey;

    public GoogleSafeBrowsingService(@Qualifier("googleSafeBrowsingClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public SafeBrowsingResult checkUrl(String url) {
        try {
            Map<String, Object> requestBody = buildRequestBody(url);

            Map<String, Object> response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/threatMatches:find")
                    .queryParam("key", apiKey)
                    .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Error Google Safe Browsing API: {} - {}", ex.getStatusCode(), ex.getMessage());
                    return Mono.empty();
                })
                .block();

            return parseResponse(response);

        } catch (Exception e) {
            log.error("Error al consultar Google Safe Browsing: {}", e.getMessage());
            return SafeBrowsingResult.error();
        }
    }

    private Map<String, Object> buildRequestBody(String url) {
        List<String> threatTypes = List.of(
            "MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"
        );
        List<String> platformTypes = List.of("ANY_PLATFORM");
        List<String> threatEntryTypes = List.of("URL");

        Map<String, Object> urlEntry = Map.of("url", url);
        Map<String, Object> threatInfo = Map.of(
            "threatTypes", threatTypes,
            "platformTypes", platformTypes,
            "threatEntryTypes", threatEntryTypes,
            "threatEntries", List.of(urlEntry)
        );

        Map<String, Object> client = Map.of(
            "clientId", "phishware",
            "clientVersion", "1.0.0"
        );

        return Map.of("client", client, "threatInfo", threatInfo);
    }

    @SuppressWarnings("unchecked")
    private SafeBrowsingResult parseResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("matches")) {
            return SafeBrowsingResult.safe();
        }

        List<Map<String, Object>> matches = (List<Map<String, Object>>) response.get("matches");
        if (matches == null || matches.isEmpty()) {
            return SafeBrowsingResult.safe();
        }

        List<String> threats = new ArrayList<>();
        String highestThreat = null;

        for (Map<String, Object> match : matches) {
            String threatType = (String) match.get("threatType");
            if (threatType != null) {
                threats.add(threatType);
                highestThreat = threatType;
            }
        }

        return SafeBrowsingResult.dangerous(threats, highestThreat);
    }

    public record SafeBrowsingResult(
        boolean isSafe,
        boolean isError,
        List<String> threats,
        String primaryThreatType
    ) {
        static SafeBrowsingResult safe() {
            return new SafeBrowsingResult(true, false, List.of(), null);
        }

        static SafeBrowsingResult dangerous(List<String> threats, String primaryThreat) {
            return new SafeBrowsingResult(false, false, threats, primaryThreat);
        }

        static SafeBrowsingResult error() {
            return new SafeBrowsingResult(true, true, List.of(), null);
        }
    }
}

package com.phishware.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VirusTotalServiceTest {

    private final VirusTotalService service =
        new VirusTotalService(WebClient.builder().build());

    private static Map<String, Object> analysisResponse(String status, Map<String, Object> stats) {
        return Map.of("data", Map.of("id", "abc123", "attributes", Map.of("status", status, "stats", stats)));
    }

    @Test
    void extractAnalysisIdReadsDataId() {
        Map<String, Object> submit = Map.of("data", Map.of("id", "xyz-1", "type", "analysis"));
        assertThat(VirusTotalService.extractAnalysisId(submit)).isEqualTo("xyz-1");
        assertThat(VirusTotalService.extractAnalysisId(Map.of())).isNull();
        assertThat(VirusTotalService.extractAnalysisId(null)).isNull();
    }

    @Test
    void completedStatusIsDetected() {
        assertThat(VirusTotalService.isAnalysisCompleted(
            analysisResponse("completed", Map.of()))).isTrue();
        assertThat(VirusTotalService.isAnalysisCompleted(
            analysisResponse("queued", Map.of()))).isFalse();
        assertThat(VirusTotalService.isAnalysisCompleted(Map.of())).isFalse();
    }

    @Test
    void statsMapToThreatLevels() {
        Map<String, Object> malicious = analysisResponse("completed",
            Map.of("malicious", 12, "suspicious", 1, "harmless", 70, "undetected", 5));
        VirusTotalService.VirusTotalResult r = VirusTotalService.fromAnalysisStats(malicious);
        assertThat(r.isError()).isFalse();
        assertThat(r.threatLevel())
            .isEqualTo(VirusTotalService.VirusTotalResult.ThreatLevel.MALICIOUS);
        assertThat(r.maliciousCount()).isEqualTo(12);

        Map<String, Object> clean = analysisResponse("completed",
            Map.of("malicious", 0, "suspicious", 0, "harmless", 80, "undetected", 8));
        VirusTotalService.VirusTotalResult ok = VirusTotalService.fromAnalysisStats(clean);
        assertThat(ok.threatLevel())
            .isEqualTo(VirusTotalService.VirusTotalResult.ThreatLevel.SAFE);

        assertThat(VirusTotalService.fromAnalysisStats(Map.of()).isError()).isTrue();
        assertThat(VirusTotalService.fromAnalysisStats(null).isError()).isTrue();
    }

    @Test
    void unconfiguredServiceReturnsError() {
        assertThat(service.isConfigured()).isFalse();
        assertThat(service.analyzeUrl("https://example.com").isError()).isTrue();
    }
}

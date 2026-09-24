package com.phishware.controller;

import com.phishware.entity.enums.RiskLevel;
import com.phishware.security.NistThreatClassifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/public/url")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Público", description = "Endpoints sin autenticación — extensión del navegador y uso externo")
public class PublicUrlCheckController {

    private final NistThreatClassifier nistClassifier;

    @GetMapping("/check")
    @Operation(summary = "Verificación rápida heurística", description = "Análisis local sin APIs externas. Usado por la extensión PHISWARE.")
    public ResponseEntity<QuickCheckResponse> check(@RequestParam String url) {
        if (url == null || url.isBlank() || url.length() > 2048) {
            return ResponseEntity.badRequest().build();
        }

        String domain = extractDomain(url);
        NistThreatClassifier.HeuristicResult result = nistClassifier.classify(url, domain);

        List<String> indicatorDescriptions = result.indicators().stream()
            .map(i -> i.type() + ": " + i.description())
            .toList();

        String classification = switch (result.riskLevel()) {
            case DANGEROUS  -> "DANGEROUS";
            case SUSPICIOUS -> "SUSPICIOUS";
            default         -> "SAFE";
        };

        return ResponseEntity.ok(new QuickCheckResponse(
            url,
            domain,
            result.heuristicScore().doubleValue(),
            classification,
            result.riskLevel() != RiskLevel.SAFE,
            indicatorDescriptions,
            result.recommendations()
        ));
    }

    private String extractDomain(String url) {
        try { return new URI(url).getHost(); } catch (URISyntaxException e) { return url; }
    }

    public record QuickCheckResponse(
        String url,
        String domain,
        double riskScore,
        String classification,
        boolean isThreat,
        List<String> indicators,
        List<String> recommendations
    ) {}
}

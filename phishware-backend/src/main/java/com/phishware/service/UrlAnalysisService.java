package com.phishware.service;

import com.phishware.dto.request.UrlAnalysisRequest;
import com.phishware.dto.response.ThreatResponse;
import com.phishware.dto.response.UrlAnalysisResponse;
import com.phishware.entity.Alert;
import com.phishware.entity.Threat;
import com.phishware.entity.UrlAnalysis;
import com.phishware.entity.User;
import com.phishware.entity.enums.AnalysisSource;
import com.phishware.entity.enums.RiskLevel;
import com.phishware.entity.enums.Severity;
import com.phishware.exception.ResourceNotFoundException;
import com.phishware.exception.UrlValidationException;
import com.phishware.repository.AlertRepository;
import com.phishware.repository.UrlAnalysisRepository;
import com.phishware.repository.UserRepository;
import com.phishware.security.InputSanitizerService;
import com.phishware.security.NistThreatClassifier;
import com.phishware.security.OwaspComplianceValidator;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlAnalysisService {

    // Confianza de cada fuente local: qué tanto creemos en su score (las heurísticas tienen falsos positivos)
    private static final double URL_HEURISTIC_CONFIDENCE = 0.75;
    private static final double CONTENT_CONFIDENCE       = 0.85;
    private static final double DOMAIN_AGE_CONFIDENCE    = 0.70;
    private static final double PHISHING_THRESHOLD       = 70.0;

    private final UrlAnalysisRepository analysisRepository;
    private final UserRepository         userRepository;
    private final AlertRepository         alertRepository;
    private final GoogleSafeBrowsingService safeBrowsingService;
    private final VirusTotalService         virusTotalService;
    private final WebContentAnalyzer        contentAnalyzer;
    private final DomainAgeService          domainAgeService;
    private final GamificationService       gamificationService;
    private final AuditLogService           auditLogService;
    // OWASP + NIST
    private final InputSanitizerService     inputSanitizer;
    private final NistThreatClassifier      nistClassifier;
    private final OwaspComplianceValidator  owaspValidator;

    // Las fuentes externas (APIs, scraping, RDAP) se consultan en paralelo
    private final ExecutorService analysisExecutor = Executors.newFixedThreadPool(16);

    @PreDestroy
    void shutdownExecutor() {
        analysisExecutor.shutdownNow();
    }

    // ── Análisis principal ────────────────────────────────────────────────────

    @Transactional
    public UrlAnalysisResponse analyzeUrl(UrlAnalysisRequest request, Long userId) {
        long startTime = System.currentTimeMillis();

        // OWASP A03: Sanitización y validación de entrada
        String url = sanitizeAndValidate(request.getUrl());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String domain   = extractDomain(url);
        String protocol = extractProtocol(url);

        // OWASP A10: Protección contra SSRF
        try {
            owaspValidator.validateAgainstSsrf(url, domain);
        } catch (SecurityException e) {
            throw new UrlValidationException(e.getMessage());
        }

        // NIST ID/DE: Análisis heurístico local
        NistThreatClassifier.HeuristicResult heuristic = nistClassifier.classify(url, domain);

        // NIST DE: Fuentes externas de inteligencia de amenazas + análisis de contenido (en paralelo)
        CompletableFuture<GoogleSafeBrowsingService.SafeBrowsingResult> gsbFuture = async(
            () -> safeBrowsingService.checkUrl(url), GoogleSafeBrowsingService.SafeBrowsingResult.error(), 20);
        CompletableFuture<VirusTotalService.VirusTotalResult> vtFuture = async(
            () -> virusTotalService.analyzeUrl(url), VirusTotalService.VirusTotalResult.error(), 20);
        CompletableFuture<WebContentAnalyzer.ContentAnalysisResult> contentFuture = async(
            () -> contentAnalyzer.analyze(url),
            WebContentAnalyzer.ContentAnalysisResult.unavailable("Tiempo de espera agotado"), 15);
        CompletableFuture<DomainAgeService.DomainAgeResult> ageFuture = async(
            () -> domainAgeService.check(domain), DomainAgeService.DomainAgeResult.unavailable(), 8);

        GoogleSafeBrowsingService.SafeBrowsingResult gsbResult = gsbFuture.join();
        VirusTotalService.VirusTotalResult          vtResult  = vtFuture.join();
        WebContentAnalyzer.ContentAnalysisResult    content   = contentFuture.join();
        DomainAgeService.DomainAgeResult            age       = ageFuture.join();

        // NIST ID.RA-3: Calcular riesgo combinado (APIs + heurística + contenido + antigüedad)
        RiskAssessment assessment = calculateCombinedRisk(gsbResult, vtResult, heuristic, content, age);

        // Construir y persistir entidad
        UrlAnalysis analysis = UrlAnalysis.builder()
            .user(user)
            .originalUrl(url)
            .domain(domain)
            .protocol(protocol)
            .riskLevel(assessment.riskLevel())
            .riskScore(assessment.riskScore())
            .isPhishing(assessment.isPhishing())
            .analysisSource(AnalysisSource.COMBINED)
            .rawResponse(buildRawResponse(assessment, content, age))
            .analysisTimeMs((int) (System.currentTimeMillis() - startTime))
            .analyzedAt(LocalDateTime.now())
            .build();

        List<Threat> threats = buildThreats(analysis, gsbResult, vtResult, heuristic, content, age);
        analysis.setThreats(threats);
        analysis = analysisRepository.save(analysis);

        // NIST RS: Generar alerta si hay riesgo
        if (assessment.riskLevel() != RiskLevel.SAFE) {
            createAlert(user, analysis, assessment);
        }

        gamificationService.onUrlAnalyzed(user, assessment.riskLevel() != RiskLevel.SAFE);
        auditLogService.log(user, "URL_ANALYZED", "UrlAnalysis", analysis.getId(), true, null, null);

        return toResponse(analysis, assessment.recommendations());
    }

    @Transactional(readOnly = true)
    public Page<UrlAnalysisResponse> getUserHistory(
            Long userId, RiskLevel riskLevel, Pageable pageable) {
        return analysisRepository
            .findByUserIdWithFilters(userId, riskLevel, pageable)
            .map(a -> toResponse(a, List.of()));
    }

    // ── Validación OWASP ──────────────────────────────────────────────────────

    /**
     * OWASP A03: Injection Prevention + NIST SI-10: Input Validation
     */
    private String sanitizeAndValidate(String raw) {
        try {
            String url = inputSanitizer.sanitizeAndValidateUrl(raw);

            OwaspComplianceValidator.ValidationResult check =
                owaspValidator.validateUrlForInjection(url);
            if (!check.valid()) {
                throw new UrlValidationException("URL rechazada (OWASP A03): " + check.firstViolation());
            }
            return url;
        } catch (SecurityException | IllegalArgumentException e) {
            throw new UrlValidationException(e.getMessage());
        }
    }

    // ── Cálculo de riesgo combinado ───────────────────────────────────────────

    /**
     * NIST ID.RA-3: Threats, vulnerabilities, likelihoods, and impacts are used to determine risk.
     *
     * Cada fuente aporta una probabilidad independiente de que la URL sea maliciosa
     * (score de la fuente × confianza de la fuente) y se combinan con noisy-OR:
     *
     *     riesgo = 1 − Π (1 − p_i)
     *
     * Ventajas frente a pesos fijos: una señal fuerte nunca se diluye por fuentes que
     * no encontraron nada, varias señales débiles se refuerzan entre sí, y una fuente
     * caída o no configurada simplemente no participa (no hay que redistribuir pesos).
     */
    private RiskAssessment calculateCombinedRisk(
            GoogleSafeBrowsingService.SafeBrowsingResult gsb,
            VirusTotalService.VirusTotalResult vt,
            NistThreatClassifier.HeuristicResult heuristic,
            WebContentAnalyzer.ContentAnalysisResult content,
            DomainAgeService.DomainAgeResult age) {

        Map<String, Double> breakdown = new LinkedHashMap<>();
        List<String> recommendations = new ArrayList<>();
        boolean isPhishing = false;

        // Fuente 1 — Google Safe Browsing (listas negras confirmadas)
        if (!gsb.isError()) {
            double p = gsb.isSafe() ? 0.0 : 0.95;
            breakdown.put("googleSafeBrowsing", p);
            if (!gsb.isSafe()) {
                isPhishing = true;
                recommendations.add("Google Safe Browsing marcó este sitio como peligroso.");
            }
        }

        // Fuente 2 — VirusTotal (consenso de motores antivirus)
        if (!vt.isError()) {
            double p = 0.0;
            if (vt.threatLevel() == VirusTotalService.VirusTotalResult.ThreatLevel.MALICIOUS) {
                p = Math.min(0.95, 0.5 + vt.maliciousPercentage() / 100.0);
                isPhishing = true;
                recommendations.add(vt.maliciousCount() + "/" + vt.totalEngines()
                    + " motores antivirus detectaron malware.");
            } else if (vt.threatLevel() == VirusTotalService.VirusTotalResult.ThreatLevel.SUSPICIOUS) {
                p = Math.min(0.4, vt.suspiciousCount() * 0.08);
                recommendations.add("Motores de análisis marcaron el sitio como sospechoso.");
            }
            breakdown.put("virusTotal", p);
        }

        // Fuente 3 — Heurística léxica de la URL (siempre disponible)
        breakdown.put("urlHeuristic", heuristic.heuristicScore().doubleValue() / 100.0 * URL_HEURISTIC_CONFIDENCE);
        if (heuristic.hasThreats()) {
            recommendations.addAll(heuristic.recommendations());
        }

        // Fuente 4 — Contenido de la página (scraping)
        if (content.available()) {
            breakdown.put("pageContent", content.score() / 100.0 * CONTENT_CONFIDENCE);
            content.indicators().stream()
                .filter(i -> i.severity() == Severity.HIGH || i.severity() == Severity.CRITICAL)
                .forEach(i -> recommendations.add("Contenido: " + i.description()));
        }

        // Fuente 5 — Antigüedad del dominio (RDAP)
        if (age.available()) {
            breakdown.put("domainAge", age.score() / 100.0 * DOMAIN_AGE_CONFIDENCE);
            age.indicators().forEach(i -> recommendations.add("Dominio: " + i.description()));
        }

        double notMalicious = 1.0;
        for (double p : breakdown.values()) notMalicious *= (1.0 - p);
        double score = Math.min(100.0, (1.0 - notMalicious) * 100.0);
        score = Math.round(score * 100.0) / 100.0;

        if (score >= PHISHING_THRESHOLD) isPhishing = true;

        // Recomendaciones base según nivel
        if (score >= 50) {
            recommendations.add("No ingrese credenciales ni datos personales en este sitio.");
            recommendations.add("Cierre esta pestaña inmediatamente.");
        } else if (score >= 20) {
            recommendations.add("Proceda con precaución — verifique la URL antes de continuar.");
        } else {
            recommendations.add("El sitio parece seguro según los análisis realizados.");
        }

        RiskLevel level;
        if (score >= 50) {
            level = RiskLevel.DANGEROUS;
        } else if (score >= 20) {
            level = RiskLevel.SUSPICIOUS;
        } else {
            level = RiskLevel.SAFE;
        }

        Map<String, Double> breakdownPct = new LinkedHashMap<>();
        breakdown.forEach((k, v) -> breakdownPct.put(k, Math.round(v * 10000.0) / 100.0));

        return new RiskAssessment(level, BigDecimal.valueOf(score), isPhishing, recommendations, breakdownPct);
    }

    private Map<String, Object> buildRawResponse(
            RiskAssessment assessment,
            WebContentAnalyzer.ContentAnalysisResult content,
            DomainAgeService.DomainAgeResult age) {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("scoreBreakdown", assessment.breakdown());
        if (content.available()) {
            raw.put("content", content.evidence());
        } else {
            raw.put("contentUnavailable", content.unavailableReason());
        }
        if (age.available()) {
            raw.put("domainRegisteredOn", age.registeredOn().toString());
            raw.put("domainAgeDays", age.ageDays());
        }
        return raw;
    }

    private <T> CompletableFuture<T> async(Supplier<T> task, T fallback, int timeoutSeconds) {
        return CompletableFuture.supplyAsync(task, analysisExecutor)
            .completeOnTimeout(fallback, timeoutSeconds, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                log.warn("Fuente de análisis falló: {}", ex.getMessage());
                return fallback;
            });
    }

    // ── Construcción de amenazas ──────────────────────────────────────────────

    private List<Threat> buildThreats(
            UrlAnalysis analysis,
            GoogleSafeBrowsingService.SafeBrowsingResult gsb,
            VirusTotalService.VirusTotalResult vt,
            NistThreatClassifier.HeuristicResult heuristic,
            WebContentAnalyzer.ContentAnalysisResult content,
            DomainAgeService.DomainAgeResult age) {

        List<Threat> threats = new ArrayList<>();

        // Amenazas de Google Safe Browsing
        for (String threatType : gsb.threats()) {
            threats.add(Threat.builder()
                .analysis(analysis)
                .threatType(threatType)
                .description("Amenaza detectada por Google Safe Browsing API [NIST DE]")
                .severity(Severity.HIGH)
                .source("GOOGLE_SAFE_BROWSING")
                .build());
        }

        // Amenazas de VirusTotal
        if (!vt.isError()) {
            if (vt.threatLevel() == VirusTotalService.VirusTotalResult.ThreatLevel.MALICIOUS) {
                threats.add(Threat.builder()
                    .analysis(analysis)
                    .threatType("MALWARE")
                    .description(vt.maliciousCount() + "/" + vt.totalEngines()
                        + " motores detectaron malware [VirusTotal]")
                    .severity(Severity.CRITICAL)
                    .source("VIRUS_TOTAL")
                    .build());
            } else if (vt.threatLevel() == VirusTotalService.VirusTotalResult.ThreatLevel.SUSPICIOUS) {
                threats.add(Threat.builder()
                    .analysis(analysis)
                    .threatType("SUSPICIOUS")
                    .description(vt.suspiciousCount() + "/" + vt.totalEngines()
                        + " motores marcaron como sospechoso")
                    .severity(Severity.MEDIUM)
                    .source("VIRUS_TOTAL")
                    .build());
            }
        }

        // Amenazas heurísticas NIST ID/DE, de contenido (scraping) y de antigüedad del dominio
        addIndicatorThreats(threats, analysis, heuristic.indicators(), "HEURISTIC_NIST");
        addIndicatorThreats(threats, analysis, content.indicators(), "CONTENT_SCRAPING");
        addIndicatorThreats(threats, analysis, age.indicators(), "DOMAIN_INTEL");

        return threats;
    }

    private void addIndicatorThreats(List<Threat> threats, UrlAnalysis analysis,
                                     List<NistThreatClassifier.ThreatIndicator> indicators, String source) {
        for (NistThreatClassifier.ThreatIndicator indicator : indicators) {
            if (indicator.severity() == Severity.HIGH || indicator.severity() == Severity.CRITICAL
                    || indicator.severity() == Severity.MEDIUM) {
                threats.add(Threat.builder()
                    .analysis(analysis)
                    .threatType(indicator.type())
                    .description(indicator.description()
                        + " [NIST " + indicator.nistFunction().name() + "]")
                    .severity(indicator.severity())
                    .source(source)
                    .build());
            }
        }
    }

    // ── Alerta NIST RS ────────────────────────────────────────────────────────

    private void createAlert(User user, UrlAnalysis analysis, RiskAssessment assessment) {
        String title = assessment.riskLevel() == RiskLevel.DANGEROUS
            ? "⛔ Sitio Peligroso Detectado"
            : "⚠️ Sitio Sospechoso";

        Severity severity = assessment.riskLevel() == RiskLevel.DANGEROUS
            ? Severity.CRITICAL
            : Severity.MEDIUM;

        Alert alert = Alert.builder()
            .user(user)
            .analysis(analysis)
            .title(title)
            .message("La URL analizada presenta características asociadas a "
                + (assessment.isPhishing() ? "phishing" : "contenido sospechoso") + ". "
                + "[NIST CSF RS.CO-2: Incidents are reported]")
            .alertType(assessment.isPhishing() ? "PHISHING" : "SUSPICIOUS_REDIRECT")
            .severity(severity)
            .recommendations(assessment.recommendations())
            .build();

        alertRepository.save(alert);
    }

    // ── Mapeo a DTO ───────────────────────────────────────────────────────────

    private UrlAnalysisResponse toResponse(UrlAnalysis analysis, List<String> recommendations) {
        List<ThreatResponse> threatResponses = analysis.getThreats().stream()
            .map(t -> ThreatResponse.builder()
                .id(t.getId())
                .threatType(t.getThreatType())
                .description(t.getDescription())
                .severity(t.getSeverity())
                .source(t.getSource())
                .detectedAt(t.getDetectedAt())
                .build())
            .collect(Collectors.toList());

        String riskMessage = getRiskMessage(analysis.getRiskLevel());

        return UrlAnalysisResponse.builder()
            .id(analysis.getId())
            .uuid(analysis.getUuid())
            .originalUrl(analysis.getOriginalUrl())
            .domain(analysis.getDomain())
            .riskLevel(analysis.getRiskLevel())
            .riskScore(analysis.getRiskScore())
            .isPhishing(analysis.isPhishing())
            .analysisSource(analysis.getAnalysisSource())
            .threats(threatResponses)
            .analysisTimeMs(analysis.getAnalysisTimeMs())
            .analyzedAt(analysis.getAnalyzedAt())
            .riskMessage(riskMessage)
            .recommendations(recommendations)
            .scoreBreakdown(extractBreakdown(analysis))
            .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractBreakdown(UrlAnalysis analysis) {
        if (analysis.getRawResponse() == null) return null;
        Object breakdown = analysis.getRawResponse().get("scoreBreakdown");
        return breakdown instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    private String getRiskMessage(RiskLevel level) {
        if (level == RiskLevel.DANGEROUS)  return "⛔ Este sitio es potencialmente peligroso.";
        if (level == RiskLevel.SUSPICIOUS) return "⚠️ Este sitio presenta características sospechosas.";
        return "✅ Este sitio parece seguro.";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String extractDomain(String url) {
        try { return new URI(url).getHost(); } catch (URISyntaxException e) { return url; }
    }

    private String extractProtocol(String url) {
        try { return new URI(url).getScheme(); } catch (URISyntaxException e) { return "unknown"; }
    }

    // ── Record interno ────────────────────────────────────────────────────────

    private record RiskAssessment(
        RiskLevel riskLevel,
        BigDecimal riskScore,
        boolean isPhishing,
        List<String> recommendations,
        Map<String, Double> breakdown
    ) {}
}

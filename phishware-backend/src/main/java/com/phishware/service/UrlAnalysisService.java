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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlAnalysisService {

    private final UrlAnalysisRepository analysisRepository;
    private final UserRepository         userRepository;
    private final AlertRepository         alertRepository;
    private final GoogleSafeBrowsingService safeBrowsingService;
    private final VirusTotalService         virusTotalService;
    private final GamificationService       gamificationService;
    private final AuditLogService           auditLogService;
    // OWASP + NIST
    private final InputSanitizerService     inputSanitizer;
    private final NistThreatClassifier      nistClassifier;
    private final OwaspComplianceValidator  owaspValidator;

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

        // NIST DE: Fuentes externas de inteligencia de amenazas
        GoogleSafeBrowsingService.SafeBrowsingResult gsbResult = safeBrowsingService.checkUrl(url);
        VirusTotalService.VirusTotalResult          vtResult  = virusTotalService.analyzeUrl(url);

        // NIST ID.RA-3: Calcular riesgo combinado (APIs + heurística)
        RiskAssessment assessment = calculateCombinedRisk(gsbResult, vtResult, heuristic);

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
            .analysisTimeMs((int) (System.currentTimeMillis() - startTime))
            .analyzedAt(LocalDateTime.now())
            .build();

        List<Threat> threats = buildThreats(analysis, gsbResult, vtResult, heuristic);
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
     * NIST ID.RA-3: Threats, vulnerabilities, likelihoods, and impacts are used to
     * determine risk. Pesos dinámicos: GSB(60%) + VT(30%) + Heurística(10%) cuando
     * todas las fuentes están disponibles. Si una falla, su peso se redistribuye.
     */
    private RiskAssessment calculateCombinedRisk(
            GoogleSafeBrowsingService.SafeBrowsingResult gsb,
            VirusTotalService.VirusTotalResult vt,
            NistThreatClassifier.HeuristicResult heuristic) {

        boolean gsbAvailable = !gsb.isError();
        boolean vtAvailable  = !vt.isError();

        // Pesos dinámicos según disponibilidad de APIs externas
        double gsbWeight, vtWeight, heuristicWeight;
        if (gsbAvailable && vtAvailable) {
            gsbWeight = 0.60; vtWeight = 0.30; heuristicWeight = 0.10;
        } else if (gsbAvailable) {
            gsbWeight = 0.70; vtWeight = 0.00; heuristicWeight = 0.30;
        } else if (vtAvailable) {
            gsbWeight = 0.00; vtWeight = 0.60; heuristicWeight = 0.40;
        } else {
            // Sin APIs externas: heurística es la única fuente → peso completo
            gsbWeight = 0.00; vtWeight = 0.00; heuristicWeight = 1.00;
        }

        double score = 0.0;
        boolean isPhishing = false;
        List<String> recommendations = new ArrayList<>();

        // Fuente 1 — Google Safe Browsing
        if (gsbAvailable && !gsb.isSafe()) {
            score += 100.0 * gsbWeight;
            isPhishing = true;
            recommendations.add("Google Safe Browsing marcó este sitio como peligroso.");
        }

        // Fuente 2 — VirusTotal
        if (vtAvailable) {
            if (vt.threatLevel() == VirusTotalService.VirusTotalResult.ThreatLevel.MALICIOUS) {
                double vtContrib = Math.min(100.0, vt.maliciousPercentage()) * vtWeight;
                score += vtContrib;
                isPhishing = true;
                recommendations.add(vt.maliciousCount() + "/" + vt.totalEngines()
                    + " motores antivirus detectaron malware.");
            } else if (vt.threatLevel() == VirusTotalService.VirusTotalResult.ThreatLevel.SUSPICIOUS) {
                double vtContrib = Math.min(50.0, vt.suspiciousCount() * 5.0) * vtWeight;
                score += vtContrib;
                recommendations.add("Motores de análisis marcaron el sitio como sospechoso.");
            }
        }

        // Fuente 3 — Heurística NIST (peso dinámico)
        double heuristicContrib = heuristic.heuristicScore().doubleValue() * heuristicWeight;
        score += heuristicContrib;
        if (heuristic.hasThreats()) {
            recommendations.addAll(heuristic.recommendations());
        }

        // Normalizar a [0-100]
        score = Math.min(score, 100.0);

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

        return new RiskAssessment(level, BigDecimal.valueOf(score), isPhishing, recommendations);
    }

    // ── Construcción de amenazas ──────────────────────────────────────────────

    private List<Threat> buildThreats(
            UrlAnalysis analysis,
            GoogleSafeBrowsingService.SafeBrowsingResult gsb,
            VirusTotalService.VirusTotalResult vt,
            NistThreatClassifier.HeuristicResult heuristic) {

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

        // Amenazas heurísticas NIST ID/DE
        for (NistThreatClassifier.ThreatIndicator indicator : heuristic.indicators()) {
            if (indicator.severity() == Severity.HIGH || indicator.severity() == Severity.CRITICAL
                    || indicator.severity() == Severity.MEDIUM) {
                threats.add(Threat.builder()
                    .analysis(analysis)
                    .threatType(indicator.type())
                    .description(indicator.description()
                        + " [NIST " + indicator.nistFunction().name() + "]")
                    .severity(indicator.severity())
                    .source("HEURISTIC_NIST")
                    .build());
            }
        }

        return threats;
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
            .build();
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
        List<String> recommendations
    ) {}
}

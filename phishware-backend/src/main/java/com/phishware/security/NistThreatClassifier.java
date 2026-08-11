package com.phishware.security;

import com.phishware.entity.enums.RiskLevel;
import com.phishware.entity.enums.Severity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * NIST Cybersecurity Framework (CSF) — Función IDENTIFY (ID)
 * NIST SP 800-30: Risk Assessment
 *
 * Implementa el motor de clasificación de amenazas alineado con NIST CSF.
 * Mapea las amenazas detectadas a las funciones del NIST CSF:
 *
 *  ID (Identify)  → Análisis inicial de la URL
 *  PR (Protect)   → Validaciones preventivas
 *  DE (Detect)    → Detección de indicadores de phishing
 *  RS (Respond)   → Generación de alertas y recomendaciones
 *  RC (Recover)   → Módulo educativo para recuperación
 */
@Component
public class NistThreatClassifier {

    // ── Indicadores Heurísticos de Phishing ─────────────────────────────────

    // Typosquatting de marcas conocidas (NIST ID.RA-2: Threat intelligence)
    private static final Pattern BRAND_TYPOSQUATTING = Pattern.compile(
        "(paypa[^l]|amaz[o0]n|g[o0]{2}gle|faceb[o0]{2}k|micr[o0]s[o0]ft|" +
        "app[l1]e|net[f1]lix|bank[o0]f|we[l1][l1]sfarg[o0])",
        Pattern.CASE_INSENSITIVE
    );

    // Subdominios sospechosos que intentan imitar legitimidad
    private static final Pattern SUSPICIOUS_SUBDOMAIN = Pattern.compile(
        "^(secure|login|account|verify|update|banking|confirm|webscr|signin|" +
        "customer|support|help|service|paypal|amazon|apple|google|microsoft)\\.",
        Pattern.CASE_INSENSITIVE
    );

    // TLDs frecuentemente abusados para phishing
    private static final List<String> HIGH_RISK_TLDS = List.of(
        ".tk", ".ml", ".ga", ".cf", ".gq", ".xyz", ".top", ".click",
        ".loan", ".win", ".download", ".stream", ".party", ".review"
    );

    // Palabras clave de phishing en la URL
    private static final Pattern PHISHING_KEYWORDS = Pattern.compile(
        "(verify|confirm|login|signin|update|secure|account|password|credential|" +
        "banking|wallet|recovery|suspend|blocked|frozen|unusual|validate)",
        Pattern.CASE_INSENSITIVE
    );

    // URLs excesivamente largas con muchos parámetros
    private static final int SUSPICIOUS_URL_LENGTH = 150;

    // ── NIST ID: Identificar ─────────────────────────────────────────────────

    /**
     * NIST CSF ID.RA-2: Cyber threat intelligence is received from information sharing forums.
     * Analiza heurísticamente una URL para identificar indicadores de phishing.
     */
    public HeuristicResult classify(String url, String domain) {
        List<ThreatIndicator> indicators = new ArrayList<>();
        double heuristicScore = 0.0;

        // 1. Typosquatting de marcas
        if (domain != null && BRAND_TYPOSQUATTING.matcher(domain).find()) {
            indicators.add(new ThreatIndicator(
                "TYPOSQUATTING",
                "El dominio imita una marca conocida con errores tipográficos",
                Severity.HIGH,
                30.0,
                NistFunction.IDENTIFY
            ));
            heuristicScore += 30.0;
        }

        // 2. Subdominio sospechoso
        if (domain != null && SUSPICIOUS_SUBDOMAIN.matcher(domain).find()) {
            indicators.add(new ThreatIndicator(
                "SUSPICIOUS_SUBDOMAIN",
                "El subdominio usa términos que imitan servicios legítimos (login, secure, verify)",
                Severity.MEDIUM,
                20.0,
                NistFunction.DETECT
            ));
            heuristicScore += 20.0;
        }

        // 3. TLD de alto riesgo
        String urlLower = url.toLowerCase();
        for (String tld : HIGH_RISK_TLDS) {
            if (urlLower.contains(tld + "/") || urlLower.endsWith(tld)) {
                indicators.add(new ThreatIndicator(
                    "HIGH_RISK_TLD",
                    "El dominio usa un TLD frecuentemente asociado a actividades maliciosas: " + tld,
                    Severity.MEDIUM,
                    15.0,
                    NistFunction.IDENTIFY
                ));
                heuristicScore += 15.0;
                break;
            }
        }

        // 4. Palabras clave de phishing en la URL
        if (PHISHING_KEYWORDS.matcher(url).find() && !isLikelySafeDomain(domain)) {
            indicators.add(new ThreatIndicator(
                "PHISHING_KEYWORDS",
                "La URL contiene términos frecuentemente usados en páginas de phishing",
                Severity.LOW,
                10.0,
                NistFunction.DETECT
            ));
            heuristicScore += 10.0;
        }

        // 5. URL excesivamente larga (técnica de ofuscación)
        if (url.length() > SUSPICIOUS_URL_LENGTH) {
            indicators.add(new ThreatIndicator(
                "EXCESSIVE_URL_LENGTH",
                "URL inusualmente larga (" + url.length() + " chars) — posible técnica de ofuscación",
                Severity.LOW,
                5.0,
                NistFunction.DETECT
            ));
            heuristicScore += 5.0;
        }

        // 6. IP en lugar de dominio (técnica directa de phishing)
        if (domain != null && domain.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
            indicators.add(new ThreatIndicator(
                "IP_AS_DOMAIN",
                "La URL usa una dirección IP en lugar de un nombre de dominio",
                Severity.HIGH,
                35.0,
                NistFunction.IDENTIFY
            ));
            heuristicScore += 35.0;
        }

        // 7. Múltiples subdominios (técnica de evasión)
        if (domain != null && domain.chars().filter(c -> c == '.').count() > 3) {
            indicators.add(new ThreatIndicator(
                "EXCESSIVE_SUBDOMAINS",
                "El dominio tiene demasiados niveles de subdominio — técnica de evasión",
                Severity.MEDIUM,
                15.0,
                NistFunction.DETECT
            ));
            heuristicScore += 15.0;
        }

        // Calcular nivel de riesgo heurístico
        RiskLevel heuristicRisk = calculateRisk(heuristicScore);

        return new HeuristicResult(
            heuristicRisk,
            BigDecimal.valueOf(Math.min(heuristicScore, 100.0)),
            indicators,
            generateNistRecommendations(heuristicRisk, indicators)
        );
    }

    private boolean isLikelySafeDomain(String domain) {
        if (domain == null) return false;
        List<String> safeSuffixes = List.of(
            "google.com", "microsoft.com", "apple.com", "amazon.com",
            "github.com", "stackoverflow.com", "wikipedia.org"
        );
        String d = domain.toLowerCase();
        return safeSuffixes.stream().anyMatch(d::endsWith);
    }

    private RiskLevel calculateRisk(double score) {
        if (score >= 40) return RiskLevel.DANGEROUS;
        if (score >= 15) return RiskLevel.SUSPICIOUS;
        return RiskLevel.SAFE;
    }

    /**
     * NIST CSF RS.CO-3: Information is shared consistent with response plans.
     * Genera recomendaciones basadas en el nivel de riesgo y los indicadores.
     */
    private List<String> generateNistRecommendations(
            RiskLevel risk, List<ThreatIndicator> indicators) {

        List<String> recs = new ArrayList<>();

        if (risk == RiskLevel.DANGEROUS) {
            recs.add("NIST CSF RS: No acceda a este sitio ni ingrese ningún dato personal.");
            recs.add("NIST CSF RS: Si ya ingresó datos, cambie su contraseña inmediatamente.");
            recs.add("NIST CSF RC: Revise el módulo educativo sobre phishing en PHISHWARE.");
        } else if (risk == RiskLevel.SUSPICIOUS) {
            recs.add("NIST CSF PR: Verifique la URL completa antes de continuar.");
            recs.add("NIST CSF PR: Contacte directamente a la organización por canales oficiales.");
            recs.add("NIST CSF DE: Reporte este sitio si confirma que es fraudulento.");
        }

        for (ThreatIndicator ind : indicators) {
            if (ind.severity() == Severity.HIGH || ind.severity() == Severity.CRITICAL) {
                recs.add("Indicador crítico: " + ind.description());
            }
        }

        if (recs.isEmpty()) {
            recs.add("NIST CSF ID: El sitio no presenta indicadores heurísticos de phishing.");
        }

        return recs;
    }

    // ── Records y Enums de dominio ────────────────────────────────────────────

    public enum NistFunction {
        IDENTIFY,   // ID — Identificar activos y riesgos
        PROTECT,    // PR — Implementar salvaguardas
        DETECT,     // DE — Detectar eventos de ciberseguridad
        RESPOND,    // RS — Responder a incidentes detectados
        RECOVER     // RC — Recuperarse de incidentes
    }

    public record ThreatIndicator(
        String type,
        String description,
        Severity severity,
        double scoreContribution,
        NistFunction nistFunction
    ) {}

    public record HeuristicResult(
        RiskLevel riskLevel,
        BigDecimal heuristicScore,
        List<ThreatIndicator> indicators,
        List<String> recommendations
    ) {
        public boolean hasThreats() {
            return riskLevel != RiskLevel.SAFE || !indicators.isEmpty();
        }
    }
}

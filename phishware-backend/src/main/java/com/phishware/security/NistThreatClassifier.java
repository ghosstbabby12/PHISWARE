package com.phishware.security;

import com.phishware.entity.enums.RiskLevel;
import com.phishware.entity.enums.Severity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

    // Palabras de phishing en el nombre del dominio (no solo subdominios punteados)
    private static final Pattern PHISHING_TERMS_IN_DOMAIN = Pattern.compile(
        "(secure[-.]login|login[-.]secure|verify[-.]account|account[-.]verify|" +
        "secure[-.]account|confirm[-.]identity|update[-.]info|signin[-.]verify|" +
        "banking[-.]update|password[-.]reset|credential|webscr)",
        Pattern.CASE_INSENSITIVE
    );

    // URL shorteners — ocultan el destino real
    private static final Pattern URL_SHORTENER = Pattern.compile(
        "^(bit\\.ly|tinyurl\\.com|t\\.co|goo\\.gl|ow\\.ly|buff\\.ly|" +
        "short\\.link|rb\\.gy|cutt\\.ly|is\\.gd|v\\.gd|shorturl\\.at|tiny\\.cc|s\\.id)$",
        Pattern.CASE_INSENSITIVE
    );

    // Subdominios sospechosos que intentan imitar legitimidad
    private static final Pattern SUSPICIOUS_SUBDOMAIN = Pattern.compile(
        "^(secure|login|account|verify|update|banking|confirm|webscr|signin|" +
        "customer|support|help|service|paypal|amazon|apple|google|microsoft)\\.",
        Pattern.CASE_INSENSITIVE
    );

    // TLDs frecuentemente abusados para phishing (Spamhaus / Interisle reports)
    private static final List<String> HIGH_RISK_TLDS = List.of(
        ".tk", ".ml", ".ga", ".cf", ".gq", ".xyz", ".top", ".click",
        ".loan", ".win", ".download", ".stream", ".party", ".review",
        ".icu", ".cyou", ".buzz", ".rest", ".monster", ".sbs", ".cfd",
        ".zip", ".mov", ".country", ".kim", ".work", ".support"
    );

    // Hosting gratuito / DNS dinámico: cualquiera puede publicar una página en segundos
    private static final List<String> FREE_HOSTING_DOMAINS = List.of(
        "000webhostapp.com", "weebly.com", "wixsite.com", "firebaseapp.com", "web.app",
        "netlify.app", "github.io", "pages.dev", "workers.dev", "vercel.app", "glitch.me",
        "herokuapp.com", "blogspot.com", "sites.google.com", "ngrok.io", "ngrok-free.app",
        "duckdns.org", "no-ip.org", "ddns.net", "repl.co", "replit.dev", "webflow.io",
        "godaddysites.com", "square.site", "carrd.co", "framer.app", "ipfs.io"
    );

    // Parámetros típicos de redirección abierta (?url=https://..., ?next=http://...)
    private static final Pattern OPEN_REDIRECT_PARAM = Pattern.compile(
        "[?&](url|redirect|redirect_uri|redir|next|continue|dest|destination|target|return|returnto|goto)" +
        "=(https?|%68%74%74%70)",
        Pattern.CASE_INSENSITIVE
    );

    // Palabras clave de phishing en la URL
    private static final Pattern PHISHING_KEYWORDS = Pattern.compile(
        "(verify|confirm|login|signin|update|secure|account|password|credential|" +
        "banking|wallet|recovery|suspend|blocked|frozen|unusual|validate|" +
        "verificar|actualizar|cuenta|clave|bloqueo|suspendida|desbloquear)",
        Pattern.CASE_INSENSITIVE
    );

    // URLs excesivamente largas con muchos parámetros
    private static final int SUSPICIOUS_URL_LENGTH = 150;

    // ── NIST ID: Identificar ─────────────────────────────────────────────────

    /**
     * NIST CSF ID.RA-2: Cyber threat intelligence is received from information sharing forums.
     * Analiza heurísticamente una URL (solo su texto, sin visitarla) para identificar
     * indicadores de phishing. Es rápido y seguro: no hace peticiones de red.
     */
    public HeuristicResult classify(String url, String domain) {
        List<ThreatIndicator> indicators = new ArrayList<>();
        String host = domain == null ? "" : domain.toLowerCase(Locale.ROOT);
        String sld  = DomainUtils.secondLevelLabel(host);
        URI uri = parseUri(url);

        // 1-2. Suplantación de marca: nombre literal (brand squatting) o parecido (typosquatting)
        checkBrandImpersonation(host, sld).ifPresent(indicators::add);

        // 3. Términos de phishing en el propio nombre del dominio (e.g. paypal-secure-login)
        if (PHISHING_TERMS_IN_DOMAIN.matcher(host).find()) {
            indicators.add(indicator("PHISHING_DOMAIN_PATTERN",
                "El dominio combina términos de phishing típicos (secure-login, verify-account, etc.)",
                Severity.HIGH, 35.0, NistFunction.DETECT));
        }

        // 4. Subdominio sospechoso punteado (e.g. secure.bancosantander.xyz)
        if (SUSPICIOUS_SUBDOMAIN.matcher(host).find() && !BrandCatalog.isAnyOfficialDomain(host)) {
            indicators.add(indicator("SUSPICIOUS_SUBDOMAIN",
                "El subdominio usa términos que imitan servicios legítimos (login, secure, verify)",
                Severity.MEDIUM, 20.0, NistFunction.DETECT));
        }

        // 5. URL shortener — destino desconocido
        if (URL_SHORTENER.matcher(host).find()) {
            indicators.add(indicator("URL_SHORTENER",
                "La URL usa un acortador que puede ocultar un destino malicioso",
                Severity.MEDIUM, 20.0, NistFunction.DETECT));
        }

        // 6. TLD de alto riesgo (se evalúa sobre el host, no sobre toda la URL)
        HIGH_RISK_TLDS.stream().filter(host::endsWith).findFirst().ifPresent(tld ->
            indicators.add(indicator("HIGH_RISK_TLD",
                "El dominio usa un TLD frecuentemente asociado a actividades maliciosas: " + tld,
                Severity.MEDIUM, 15.0, NistFunction.IDENTIFY)));

        // 7. Palabras clave de phishing en la URL
        if (PHISHING_KEYWORDS.matcher(url).find() && !isLikelySafeDomain(host)) {
            indicators.add(indicator("PHISHING_KEYWORDS",
                "La URL contiene términos frecuentemente usados en páginas de phishing",
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }

        // 8. URL excesivamente larga (técnica de ofuscación)
        if (url.length() > SUSPICIOUS_URL_LENGTH) {
            indicators.add(indicator("EXCESSIVE_URL_LENGTH",
                "URL inusualmente larga (" + url.length() + " chars) — posible técnica de ofuscación",
                Severity.LOW, 5.0, NistFunction.DETECT));
        }

        // 9. IP en lugar de dominio (técnica directa de phishing)
        if (DomainUtils.isIpAddress(host)) {
            indicators.add(indicator("IP_AS_DOMAIN",
                "La URL usa una dirección IP en lugar de un nombre de dominio",
                Severity.HIGH, 35.0, NistFunction.IDENTIFY));
        }

        // 10. Múltiples subdominios (técnica de evasión)
        if (host.chars().filter(c -> c == '.').count() > 3) {
            indicators.add(indicator("EXCESSIVE_SUBDOMAINS",
                "El dominio tiene demasiados niveles de subdominio — técnica de evasión",
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }

        // 11. Punycode / IDN: caracteres de otros alfabetos que se ven iguales (аpple.com con "а" cirílica)
        if (host.startsWith("xn--") || host.contains(".xn--")) {
            indicators.add(indicator("PUNYCODE_HOMOGRAPH",
                "El dominio usa caracteres internacionales (punycode) que pueden imitar visualmente a otro sitio",
                Severity.HIGH, 30.0, NistFunction.DETECT));
        }

        // 12. Símbolo @ en la autoridad: el navegador ignora todo lo anterior (https://paypal.com@evil.xyz)
        if (uri != null && uri.getRawUserInfo() != null) {
            indicators.add(indicator("USERINFO_TRICK",
                "La URL contiene '@' antes del dominio: lo que aparece antes es engañoso y se ignora",
                Severity.HIGH, 35.0, NistFunction.DETECT));
        }

        // 13. Puerto no estándar
        if (uri != null && uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443) {
            indicators.add(indicator("NON_STANDARD_PORT",
                "La URL usa un puerto poco común (" + uri.getPort() + ")",
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }

        // 14. Sin cifrado TLS
        if (uri != null && "http".equalsIgnoreCase(uri.getScheme())) {
            indicators.add(indicator("NO_HTTPS",
                "La conexión no está cifrada (HTTP): los datos viajan en texto plano",
                Severity.LOW, 10.0, NistFunction.PROTECT));
        }

        // 15. Redirección abierta o "//" embebido en la ruta
        String path = uri != null && uri.getRawPath() != null ? uri.getRawPath() : "";
        if (OPEN_REDIRECT_PARAM.matcher(url).find() || path.contains("//")) {
            indicators.add(indicator("EMBEDDED_REDIRECT",
                "La URL contiene otra URL como parámetro — posible redirección a un sitio malicioso",
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }

        // 16. Nombre de dominio aleatorio (alta entropía, típico de dominios generados por algoritmo)
        if (sld.length() >= 12 && DomainUtils.shannonEntropy(sld) > 3.6
                && !DomainUtils.isIpAddress(host)) {
            indicators.add(indicator("RANDOM_LOOKING_DOMAIN",
                "El nombre del dominio parece generado aleatoriamente",
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }

        // 17. Exceso de guiones o dígitos en el dominio (paypal-login-secure-verify.com)
        long hyphens = sld.chars().filter(c -> c == '-').count();
        long digits  = sld.chars().filter(Character::isDigit).count();
        if (hyphens >= 3 || (sld.length() > 0 && digits >= 4 && (double) digits / sld.length() > 0.3)) {
            indicators.add(indicator("DOMAIN_COMPOSITION",
                "El dominio tiene demasiados guiones o dígitos, algo poco común en sitios legítimos",
                Severity.LOW, 10.0, NistFunction.DETECT));
        }

        // 18. Hosting gratuito / DNS dinámico
        FREE_HOSTING_DOMAINS.stream().filter(d -> DomainUtils.isSameOrSubdomain(host, d)).findFirst()
            .ifPresent(d -> indicators.add(indicator("FREE_HOSTING",
                "El sitio está alojado en un servicio gratuito (" + d + ") muy usado para campañas de phishing",
                Severity.MEDIUM, 15.0, NistFunction.IDENTIFY)));

        double heuristicScore = indicators.stream().mapToDouble(ThreatIndicator::scoreContribution).sum();

        // Calcular nivel de riesgo heurístico
        RiskLevel heuristicRisk = calculateRisk(heuristicScore);

        return new HeuristicResult(
            heuristicRisk,
            BigDecimal.valueOf(Math.min(heuristicScore, 100.0)),
            List.copyOf(indicators),
            generateNistRecommendations(heuristicRisk, indicators)
        );
    }

    /**
     * Brand squatting: la marca aparece literal en un dominio no oficial (paypal-seguro.com).
     * Typosquatting: el dominio se parece a la marca tras normalizar homoglifos o con 1-2
     * letras de diferencia (paypa1.com, rnicrosoft.com, gooogle.com).
     */
    private Optional<ThreatIndicator> checkBrandImpersonation(String host, String sld) {
        if (host.isEmpty() || DomainUtils.isIpAddress(host)) return Optional.empty();

        List<String> tokens = List.of(host.split("[.-]"));
        String normalizedSld = DomainUtils.normalizeHomoglyphs(sld);

        for (BrandCatalog.Brand brand : BrandCatalog.BRANDS) {
            if (BrandCatalog.isOfficialFor(brand, host)) continue;
            String name = brand.name();

            // Nombres cortos (dhl, bbva, dian) solo cuentan como palabra completa para evitar falsos positivos
            boolean literal = name.length() >= 5 ? host.contains(name) : tokens.contains(name);
            if (literal) {
                return Optional.of(indicator("BRAND_SQUATTING",
                    "El dominio contiene la marca \"" + name + "\" pero no es su dominio oficial",
                    Severity.HIGH, 40.0, NistFunction.IDENTIFY));
            }

            if (name.length() < 5) continue;
            boolean homoglyph = normalizedSld.contains(name);
            int maxDistance = name.length() >= 9 ? 2 : 1;
            boolean nearMiss = name.length() >= 6
                && List.of(sld.split("-")).stream()
                    .anyMatch(t -> Math.abs(t.length() - name.length()) <= maxDistance
                        && DomainUtils.levenshtein(t, name) <= maxDistance);
            if (homoglyph || nearMiss) {
                return Optional.of(indicator("TYPOSQUATTING",
                    "El dominio imita a \"" + name + "\" cambiando letras o usando caracteres parecidos",
                    Severity.HIGH, 40.0, NistFunction.IDENTIFY));
            }
        }
        return Optional.empty();
    }

    private static ThreatIndicator indicator(String type, String description, Severity severity,
                                             double score, NistFunction function) {
        return new ThreatIndicator(type, description, severity, score, function);
    }

    private static URI parseUri(String url) {
        try { return new URI(url); } catch (URISyntaxException e) { return null; }
    }

    private boolean isLikelySafeDomain(String domain) {
        if (domain == null) return false;
        List<String> safeSuffixes = List.of(
            "google.com", "microsoft.com", "apple.com", "amazon.com",
            "github.com", "stackoverflow.com", "wikipedia.org"
        );
        return safeSuffixes.stream().anyMatch(s -> DomainUtils.isSameOrSubdomain(domain, s))
            || BrandCatalog.isAnyOfficialDomain(domain);
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
        public HeuristicResult {
            indicators    = List.copyOf(indicators);
            recommendations = List.copyOf(recommendations);
        }

        public boolean hasThreats() {
            return riskLevel != RiskLevel.SAFE || !indicators.isEmpty();
        }
    }
}

package com.phishware.service;

import com.phishware.entity.enums.Severity;
import com.phishware.security.BrandCatalog;
import com.phishware.security.DomainUtils;
import com.phishware.security.NistThreatClassifier.NistFunction;
import com.phishware.security.NistThreatClassifier.ThreatIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Análisis de contenido web (web scraping) — NIST CSF DE.CM: monitoreo de contenido.
 *
 * Visita la URL, extrae el HTML con Jsoup y busca las señales típicas de un kit de phishing:
 * formularios que envían credenciales a otro dominio, suplantación visual de marcas,
 * scripts ofuscados, exfiltración por Telegram/Discord, redirecciones automáticas,
 * iframes ocultos, texto de urgencia, etc. Cada señal suma puntos al score de contenido.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebContentAnalyzer {

    private static final Pattern SENSITIVE_FIELD = Pattern.compile(
        "(card|tarjeta|ccnum|cc-?number|cvv|cvc|csc|expir|vencimiento|ssn|cedula|c[eé]dula|" +
        "documento|pin\\b|otp|token|seed|mnemonic|frase|recovery.?phrase|private.?key)",
        Pattern.CASE_INSENSITIVE
    );

    private static final List<Pattern> OBFUSCATION_PATTERNS = List.of(
        Pattern.compile("\\beval\\s*\\("),
        Pattern.compile("\\batob\\s*\\("),
        Pattern.compile("\\bunescape\\s*\\("),
        Pattern.compile("String\\.fromCharCode\\s*\\("),
        Pattern.compile("document\\.write\\s*\\(\\s*(unescape|atob|decodeURIComponent)"),
        Pattern.compile("(\\\\x[0-9a-fA-F]{2}){30,}"),
        Pattern.compile("[A-Za-z0-9+/]{600,}={0,2}")
    );

    // Kits de phishing modernos envían lo robado directamente a bots de Telegram o webhooks de Discord
    private static final Pattern EXFILTRATION = Pattern.compile(
        "(api\\.telegram\\.org/bot|discord(app)?\\.com/api/webhooks|hooks\\.slack\\.com/services)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern JS_REDIRECT = Pattern.compile(
        "(?:window\\.|document\\.|top\\.)?location(?:\\.href)?\\s*(?:=|\\.replace\\(|\\.assign\\()\\s*['\"](https?://[^'\"]+)['\"]"
    );

    private static final Pattern ANTI_INSPECTION = Pattern.compile(
        "(keyCode\\s*==+\\s*123|contextmenu[\\s\\S]{0,80}preventDefault|oncontextmenu\\s*=\\s*['\"]?return false)",
        Pattern.CASE_INSENSITIVE
    );

    private static final List<String> URGENCY_PHRASES = List.of(
        // Español
        "su cuenta ha sido suspendida", "tu cuenta ha sido suspendida", "cuenta bloqueada",
        "verifique su identidad", "verifica tu identidad", "actualice sus datos", "actualiza tus datos",
        "actividad inusual", "actividad sospechosa", "dentro de 24 horas", "en las próximas 24 horas",
        "confirme su información", "confirma tu información", "evite la suspensión",
        "ha ganado", "has ganado", "reclame su premio", "reclama tu premio", "acceso restringido",
        // Inglés
        "your account has been suspended", "account suspended", "verify your identity",
        "unusual activity", "suspicious activity", "within 24 hours", "confirm your information",
        "update your payment", "your account will be closed", "you have won", "claim your prize",
        "unlock your account", "limited access"
    );

    private static final Set<String> DOWNLOAD_TYPES = Set.of(
        "application/vnd.android.package-archive", "application/x-msdownload",
        "application/x-msdos-program", "application/x-ms-installer", "application/x-apple-diskimage",
        "application/java-archive", "application/x-sh", "application/hta"
    );

    private final SafePageFetcher fetcher;

    @Value("${app.content-analysis.enabled:true}")
    private boolean enabled;

    public ContentAnalysisResult analyze(String url) {
        if (!enabled) {
            return ContentAnalysisResult.unavailable("Análisis de contenido deshabilitado");
        }

        SafePageFetcher.FetchResult fetch = fetcher.fetch(url);

        if (!fetch.isSuccess()) {
            // Un certificado TLS inválido es en sí mismo una señal de riesgo
            if (fetch.error() == SafePageFetcher.FetchError.TLS_ERROR) {
                return ContentAnalysisResult.of(List.of(new ThreatIndicator("INVALID_CERTIFICATE",
                    "El certificado HTTPS del sitio es inválido, expiró o no coincide con el dominio",
                    Severity.HIGH, 30.0, NistFunction.DETECT)), evidenceOf(fetch));
            }
            if (fetch.error() == SafePageFetcher.FetchError.TOO_MANY_REDIRECTS) {
                return ContentAnalysisResult.of(List.of(new ThreatIndicator("REDIRECT_LOOP",
                    "El sitio encadena demasiadas redirecciones — técnica usada para evadir análisis",
                    Severity.MEDIUM, 15.0, NistFunction.DETECT)), evidenceOf(fetch));
            }
            return ContentAnalysisResult.unavailable("No se pudo obtener la página: " + fetch.error());
        }

        // Descarga directa de ejecutables / APKs
        String ct = fetch.contentType().toLowerCase();
        String disposition = fetch.contentDisposition().toLowerCase();
        if (DOWNLOAD_TYPES.stream().anyMatch(ct::contains)
                || disposition.matches(".*filename=.*\\.(apk|exe|msi|scr|bat|js|vbs|hta|dmg|jar)\"?.*")) {
            return ContentAnalysisResult.of(List.of(new ThreatIndicator("EXECUTABLE_DOWNLOAD",
                "La URL descarga directamente un archivo ejecutable o aplicación",
                Severity.HIGH, 40.0, NistFunction.DETECT)), evidenceOf(fetch));
        }

        if (!fetch.isHtml()) {
            return ContentAnalysisResult.unavailable("El recurso no es HTML (" + fetch.contentType() + ")");
        }

        try {
            Document doc = Jsoup.parse(new ByteArrayInputStream(fetch.body()), null, fetch.finalUrl());
            return analyzeDocument(doc, fetch);
        } catch (IOException e) {
            log.debug("No se pudo parsear HTML de {}: {}", url, e.getMessage());
            return ContentAnalysisResult.unavailable("HTML no válido");
        }
    }

    /** Extrae las características del documento. Separado de la descarga para poder testearlo. */
    ContentAnalysisResult analyzeDocument(Document doc, SafePageFetcher.FetchResult fetch) {
        List<ThreatIndicator> indicators = new ArrayList<>();
        Map<String, Object> evidence = evidenceOf(fetch);

        String originalHost = hostOf(fetch.originalUrl());
        String finalHost    = hostOf(fetch.finalUrl());
        String finalReg     = DomainUtils.registrableDomain(finalHost);
        boolean finalIsHttps = fetch.finalUrl().toLowerCase().startsWith("https://");

        // ── 1. Redirecciones ──────────────────────────────────────────────────
        if (!DomainUtils.registrableDomain(originalHost).equals(finalReg)) {
            indicators.add(new ThreatIndicator("CROSS_DOMAIN_REDIRECT",
                "La URL redirige a otro dominio: " + finalHost,
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }
        if (fetch.redirectChain().size() >= 4) {
            indicators.add(new ThreatIndicator("LONG_REDIRECT_CHAIN",
                "La URL pasa por " + (fetch.redirectChain().size() - 1) + " redirecciones antes de llegar al destino",
                Severity.LOW, 5.0, NistFunction.DETECT));
        }

        // ── 2. Formularios y campos sensibles ─────────────────────────────────
        Elements passwordFields = doc.select("input[type=password]");
        int sensitiveFields = 0;
        for (Element input : doc.select("input, select, textarea")) {
            String attrs = String.join(" ", input.attr("name"), input.attr("id"),
                input.attr("placeholder"), input.attr("autocomplete"), input.attr("aria-label"));
            if (SENSITIVE_FIELD.matcher(attrs).find()) sensitiveFields++;
        }
        boolean asksCredentials = !passwordFields.isEmpty() || sensitiveFields > 0;
        evidence.put("passwordFields", passwordFields.size());
        evidence.put("sensitiveFields", sensitiveFields);
        evidence.put("forms", doc.select("form").size());

        boolean externalAction = false, insecureAction = false, mailtoAction = false;
        for (Element form : doc.select("form")) {
            boolean formAsksSecrets = !form.select("input[type=password]").isEmpty()
                || SENSITIVE_FIELD.matcher(form.html()).find();
            if (!formAsksSecrets) continue;

            String rawAction = form.attr("action").trim().toLowerCase();
            if (rawAction.startsWith("mailto:")) { mailtoAction = true; continue; }
            String action = form.absUrl("action");
            if (action.isEmpty()) continue;   // sin action = se envía a la misma página
            String actionReg = DomainUtils.registrableDomain(hostOf(action));
            if (!actionReg.isEmpty() && !actionReg.equals(finalReg)) externalAction = true;
            if (action.startsWith("http://")) insecureAction = true;
        }
        if (externalAction) {
            indicators.add(new ThreatIndicator("FORM_EXTERNAL_ACTION",
                "El formulario de acceso envía los datos a un dominio diferente al de la página",
                Severity.HIGH, 30.0, NistFunction.DETECT));
        }
        if (mailtoAction) {
            indicators.add(new ThreatIndicator("FORM_MAILTO",
                "El formulario envía los datos ingresados por correo electrónico",
                Severity.HIGH, 25.0, NistFunction.DETECT));
        }
        if (asksCredentials && (!finalIsHttps || insecureAction)) {
            indicators.add(new ThreatIndicator("CREDENTIALS_OVER_HTTP",
                "La página pide contraseña o datos sensibles sin cifrado HTTPS",
                Severity.HIGH, 25.0, NistFunction.PROTECT));
        }
        if (sensitiveFields > 0 && !BrandCatalog.isAnyOfficialDomain(finalHost)) {
            indicators.add(new ThreatIndicator("SENSITIVE_DATA_REQUEST",
                "La página solicita datos financieros o de identidad (tarjeta, CVV, PIN, cédula, frase semilla)",
                Severity.MEDIUM, 20.0, NistFunction.DETECT));
        }

        // ── 3. Suplantación de marca ──────────────────────────────────────────
        String title = doc.title();
        String identityText = String.join(" ", title,
            doc.select("meta[property=og:site_name]").attr("content"),
            doc.select("meta[name=application-name]").attr("content"),
            String.join(" ", doc.select("img[alt]").eachAttr("alt")));
        Optional<BrandCatalog.Brand> brand = BrandCatalog.findMentionedBrand(identityText);
        if (brand.isEmpty() && asksCredentials) {
            String body = doc.body() == null ? "" : doc.body().text();
            brand = BrandCatalog.findMentionedBrand(body.length() > 5000 ? body.substring(0, 5000) : body);
        }
        evidence.put("title", title.length() > 200 ? title.substring(0, 200) : title);

        Elements links = doc.select("a[href]");
        int totalLinks = links.size(), nullLinks = 0, externalLinks = 0, brandLinks = 0;
        for (Element a : links) {
            String href = a.attr("href").trim().toLowerCase();
            if (href.isEmpty() || href.equals("#") || href.startsWith("javascript:")) { nullLinks++; continue; }
            String linkHost = hostOf(a.absUrl("href"));
            if (linkHost.isEmpty()) continue;
            if (!DomainUtils.registrableDomain(linkHost).equals(finalReg)) externalLinks++;
            if (brand.isPresent() && BrandCatalog.isOfficialFor(brand.get(), linkHost)) brandLinks++;
        }
        // Recursos (imágenes, CSS, scripts, favicon) cargados directamente desde la marca suplantada
        int brandResources = 0;
        if (brand.isPresent()) {
            for (Element r : doc.select("img[src], script[src], link[href]")) {
                String src = r.hasAttr("src") ? r.absUrl("src") : r.absUrl("href");
                if (BrandCatalog.isOfficialFor(brand.get(), hostOf(src))) brandResources++;
            }
        }
        evidence.put("links", totalLinks);
        evidence.put("nullLinks", nullLinks);
        evidence.put("externalLinks", externalLinks);

        if (brand.isPresent() && !BrandCatalog.isOfficialFor(brand.get(), finalHost)) {
            String name = brand.get().name();
            evidence.put("brandDetected", name);
            if (asksCredentials) {
                indicators.add(new ThreatIndicator("BRAND_IMPERSONATION",
                    "La página se presenta como \"" + name + "\" y pide credenciales, pero está alojada en " + finalHost,
                    Severity.CRITICAL, 45.0, NistFunction.DETECT));
            } else if (brandResources >= 3 || (totalLinks >= 5 && (double) brandLinks / totalLinks > 0.3)) {
                indicators.add(new ThreatIndicator("CLONED_SITE",
                    "La página copia recursos y enlaces del sitio oficial de \"" + name + "\" (posible clon)",
                    Severity.MEDIUM, 20.0, NistFunction.DETECT));
            }
        }

        // ── 4. Enlaces ────────────────────────────────────────────────────────
        if (totalLinks >= 5 && (double) nullLinks / totalLinks > 0.5) {
            indicators.add(new ThreatIndicator("NULL_LINKS",
                "La mayoría de enlaces de la página no llevan a ninguna parte (" + nullLinks + "/" + totalLinks + ")",
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }
        if (asksCredentials && totalLinks >= 10 && (double) externalLinks / totalLinks > 0.8) {
            indicators.add(new ThreatIndicator("EXTERNAL_LINKS",
                "Casi todos los enlaces apuntan a otros dominios, típico de páginas clonadas",
                Severity.LOW, 10.0, NistFunction.DETECT));
        }

        // ── 5. Scripts ────────────────────────────────────────────────────────
        StringBuilder scriptText = new StringBuilder();
        for (Element s : doc.select("script:not([src])")) {
            if (scriptText.length() > 300_000) break;
            scriptText.append(s.data()).append('\n');
        }
        String scripts = scriptText.toString();
        String fullHtml = doc.outerHtml();

        long obfuscationHits = OBFUSCATION_PATTERNS.stream().filter(p -> p.matcher(scripts).find()).count();
        if (obfuscationHits >= 2) {
            indicators.add(new ThreatIndicator("OBFUSCATED_SCRIPT",
                "La página contiene JavaScript ofuscado (eval, atob, unescape, cadenas codificadas)",
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }
        if (EXFILTRATION.matcher(fullHtml).find()) {
            indicators.add(new ThreatIndicator("DATA_EXFILTRATION",
                "La página envía información a un bot de Telegram o webhook — técnica típica de kits de phishing",
                Severity.CRITICAL, 45.0, NistFunction.DETECT));
        }
        if (ANTI_INSPECTION.matcher(fullHtml).find()) {
            indicators.add(new ThreatIndicator("ANTI_INSPECTION",
                "La página bloquea clic derecho o herramientas de desarrollador para impedir su inspección",
                Severity.LOW, 5.0, NistFunction.DETECT));
        }

        // ── 6. Redirecciones automáticas (meta refresh / JS) ──────────────────
        Optional<String> autoRedirect = findAutoRedirect(doc, scripts)
            .filter(target -> !DomainUtils.registrableDomain(hostOf(target)).equals(finalReg));
        if (autoRedirect.isPresent()) {
            indicators.add(new ThreatIndicator("AUTO_REDIRECT",
                "La página redirige automáticamente a otro dominio: " + hostOf(autoRedirect.get()),
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }

        // ── 7. Iframes ocultos ────────────────────────────────────────────────
        boolean hiddenIframe = doc.select("iframe").stream().anyMatch(f -> {
            String style = f.attr("style").replace(" ", "").toLowerCase();
            return f.attr("width").matches("0|1") || f.attr("height").matches("0|1")
                || style.contains("display:none") || style.contains("visibility:hidden");
        });
        if (hiddenIframe) {
            indicators.add(new ThreatIndicator("HIDDEN_IFRAME",
                "La página contiene un iframe oculto que puede cargar contenido malicioso",
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }

        // ── 8. Lenguaje de urgencia / ingeniería social ───────────────────────
        String text = doc.body() == null ? "" : doc.body().text().toLowerCase();
        List<String> urgency = URGENCY_PHRASES.stream().filter(text::contains).toList();
        if (urgency.size() >= 2) {
            indicators.add(new ThreatIndicator("URGENCY_LANGUAGE",
                "La página usa lenguaje de urgencia o amenaza para presionar al usuario (\""
                    + urgency.get(0) + "\", \"" + urgency.get(1) + "\")",
                Severity.MEDIUM, 15.0, NistFunction.DETECT));
        }

        return ContentAnalysisResult.of(indicators, evidence);
    }

    private Optional<String> findAutoRedirect(Document doc, String scripts) {
        for (Element meta : doc.select("meta[http-equiv]")) {
            if (!"refresh".equalsIgnoreCase(meta.attr("http-equiv"))) continue;
            Matcher m = Pattern.compile("url\\s*=\\s*['\"]?([^'\";\\s]+)", Pattern.CASE_INSENSITIVE)
                .matcher(meta.attr("content"));
            if (m.find()) {
                try {
                    return Optional.of(URI.create(doc.location()).resolve(m.group(1)).toString());
                } catch (IllegalArgumentException e) {
                    return Optional.empty();
                }
            }
        }
        Matcher js = JS_REDIRECT.matcher(scripts);
        return js.find() ? Optional.of(js.group(1)) : Optional.empty();
    }

    private static Map<String, Object> evidenceOf(SafePageFetcher.FetchResult fetch) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("finalUrl", fetch.finalUrl());
        evidence.put("statusCode", fetch.statusCode());
        evidence.put("redirectChain", fetch.redirectChain());
        if (fetch.error() != null) evidence.put("fetchError", fetch.error().name());
        return evidence;
    }

    private static String hostOf(String url) {
        try {
            String host = URI.create(url).getHost();
            return host == null ? "" : host.toLowerCase();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    // ── Resultado ─────────────────────────────────────────────────────────────

    public record ContentAnalysisResult(
        boolean available,
        String unavailableReason,
        double score,
        List<ThreatIndicator> indicators,
        Map<String, Object> evidence
    ) {
        static ContentAnalysisResult of(List<ThreatIndicator> indicators, Map<String, Object> evidence) {
            double score = Math.min(100.0, indicators.stream().mapToDouble(ThreatIndicator::scoreContribution).sum());
            return new ContentAnalysisResult(true, null, score, List.copyOf(indicators), evidence);
        }

        public static ContentAnalysisResult unavailable(String reason) {
            return new ContentAnalysisResult(false, reason, 0.0, List.of(), Map.of());
        }
    }
}

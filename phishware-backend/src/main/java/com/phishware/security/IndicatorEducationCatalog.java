package com.phishware.security;

import java.util.Map;
import java.util.Set;

/**
 * Maps every heuristic indicator type to the persuasion principle it exploits
 * and to the micro-lesson that explains it. Single source of truth used by
 * the API responses and the public quick-check endpoint.
 */
public final class IndicatorEducationCatalog {

    public record EducationRef(PersuasionPrinciple principle, String microlessonSlug, String microlessonTitle) {}

    private static final EducationRef AUTHORITY_REF =
        new EducationRef(PersuasionPrinciple.AUTHORITY, "micro-principio-autoridad", "Autoridad: el disfraz favorito del phishing");
    private static final EducationRef URGENCY_REF =
        new EducationRef(PersuasionPrinciple.URGENCY, "micro-principio-escasez", "Urgencia y escasez: decidir sin pensar");
    private static final EducationRef SCARCITY_REF =
        new EducationRef(PersuasionPrinciple.SCARCITY, "micro-principio-escasez", "Urgencia y escasez: decidir sin pensar");
    private static final EducationRef TRUST_REF =
        new EducationRef(PersuasionPrinciple.TRUST, "micro-principio-simpatia", "Simpatía y confianza: el anzuelo invisible");
    private static final EducationRef SOCIAL_PROOF_REF =
        new EducationRef(PersuasionPrinciple.SOCIAL_PROOF, "micro-principio-prueba-social", "Prueba social: si todos caen, tú también");
    private static final EducationRef RECIPROCITY_REF =
        new EducationRef(PersuasionPrinciple.RECIPROCITY, "micro-principio-reciprocidad", "Reciprocidad: nada es gratis");
    private static final EducationRef COMMITMENT_REF =
        new EducationRef(PersuasionPrinciple.COMMITMENT, "micro-principio-compromiso", "Compromiso: el sí que te encadena");
    private static final EducationRef LIKING_REF =
        new EducationRef(PersuasionPrinciple.LIKING, "micro-principio-simpatia", "Simpatía y confianza: el anzuelo invisible");
    private static final EducationRef FEAR_REF =
        new EducationRef(PersuasionPrinciple.FEAR, "micro-principio-escasez", "Urgencia y escasez: decidir sin pensar");
    private static final EducationRef CURIOSITY_REF =
        new EducationRef(PersuasionPrinciple.CURIOSITY, "micro-principio-prueba-social", "Prueba social: si todos caen, tú también");
    private static final EducationRef NONE_REF =
        new EducationRef(PersuasionPrinciple.NONE, "micro-principio-prueba-social", "Prueba social: si todos caen, tú también");

    private static final Map<String, EducationRef> BY_TYPE = Map.ofEntries(
        Map.entry("BRAND_SQUATTING", AUTHORITY_REF),
        Map.entry("TYPOSQUATTING", TRUST_REF),
        Map.entry("PHISHING_DOMAIN_PATTERN", AUTHORITY_REF),
        Map.entry("SUSPICIOUS_SUBDOMAIN", TRUST_REF),
        Map.entry("URL_SHORTENER", CURIOSITY_REF),
        Map.entry("HIGH_RISK_TLD", NONE_REF),
        Map.entry("PHISHING_KEYWORDS", URGENCY_REF),
        Map.entry("EXCESSIVE_URL_LENGTH", NONE_REF),
        Map.entry("IP_AS_DOMAIN", TRUST_REF),
        Map.entry("EXCESSIVE_SUBDOMAINS", TRUST_REF),
        Map.entry("PUNYCODE_HOMOGRAPH", TRUST_REF),
        Map.entry("USERINFO_TRICK", TRUST_REF),
        Map.entry("NON_STANDARD_PORT", NONE_REF),
        Map.entry("NO_HTTPS", NONE_REF),
        Map.entry("EMBEDDED_REDIRECT", TRUST_REF),
        Map.entry("RANDOM_LOOKING_DOMAIN", NONE_REF),
        Map.entry("DOMAIN_COMPOSITION", TRUST_REF),
        Map.entry("FREE_HOSTING", TRUST_REF),
        Map.entry("INVALID_CERTIFICATE", TRUST_REF),
        Map.entry("REDIRECT_LOOP", NONE_REF),
        Map.entry("EXECUTABLE_DOWNLOAD", FEAR_REF),
        Map.entry("CROSS_DOMAIN_REDIRECT", TRUST_REF),
        Map.entry("LONG_REDIRECT_CHAIN", NONE_REF),
        Map.entry("FORM_EXTERNAL_ACTION", AUTHORITY_REF),
        Map.entry("FORM_MAILTO", NONE_REF),
        Map.entry("CREDENTIALS_OVER_HTTP", URGENCY_REF),
        Map.entry("SENSITIVE_DATA_REQUEST", AUTHORITY_REF),
        Map.entry("BRAND_IMPERSONATION", AUTHORITY_REF),
        Map.entry("CLONED_SITE", TRUST_REF),
        Map.entry("NULL_LINKS", NONE_REF),
        Map.entry("EXTERNAL_LINKS", NONE_REF),
        Map.entry("OBFUSCATED_SCRIPT", NONE_REF),
        Map.entry("DATA_EXFILTRATION", FEAR_REF),
        Map.entry("ANTI_INSPECTION", NONE_REF),
        Map.entry("AUTO_REDIRECT", URGENCY_REF),
        Map.entry("HIDDEN_IFRAME", NONE_REF),
        Map.entry("MISSING_BRAND_FAVICON", TRUST_REF),
        Map.entry("URGENCY_LANGUAGE", URGENCY_REF),
        Map.entry("VERY_NEW_DOMAIN", URGENCY_REF),
        Map.entry("NEW_DOMAIN", URGENCY_REF),
        Map.entry("RECENT_DOMAIN", NONE_REF)
    );

    private IndicatorEducationCatalog() {}

    public static EducationRef forType(String type) {
        return BY_TYPE.getOrDefault(type, NONE_REF);
    }

    public static Set<String> knownTypes() {
        return BY_TYPE.keySet();
    }
}

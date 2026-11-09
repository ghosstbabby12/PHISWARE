package com.phishware.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class IndicatorEducationCatalogTest {

    @Test
    void everyHeuristicIndicatorTypeHasAnEntry() {
        String[] types = {
            "BRAND_SQUATTING", "TYPOSQUATTING", "PHISHING_DOMAIN_PATTERN",
            "SUSPICIOUS_SUBDOMAIN", "URL_SHORTENER", "HIGH_RISK_TLD",
            "PHISHING_KEYWORDS", "EXCESSIVE_URL_LENGTH", "IP_AS_DOMAIN",
            "EXCESSIVE_SUBDOMAINS", "PUNYCODE_HOMOGRAPH", "USERINFO_TRICK",
            "NON_STANDARD_PORT", "NO_HTTPS", "EMBEDDED_REDIRECT",
            "RANDOM_LOOKING_DOMAIN", "DOMAIN_COMPOSITION", "FREE_HOSTING",
            "INVALID_CERTIFICATE", "REDIRECT_LOOP", "EXECUTABLE_DOWNLOAD",
            "CROSS_DOMAIN_REDIRECT", "LONG_REDIRECT_CHAIN", "FORM_EXTERNAL_ACTION",
            "FORM_MAILTO", "CREDENTIALS_OVER_HTTP", "SENSITIVE_DATA_REQUEST",
            "BRAND_IMPERSONATION", "CLONED_SITE", "NULL_LINKS", "EXTERNAL_LINKS",
            "OBFUSCATED_SCRIPT", "DATA_EXFILTRATION", "ANTI_INSPECTION",
            "AUTO_REDIRECT", "HIDDEN_IFRAME", "MISSING_BRAND_FAVICON", "URGENCY_LANGUAGE",
            "VERY_NEW_DOMAIN", "NEW_DOMAIN", "RECENT_DOMAIN"
        };
        for (String type : types) {
            assertThat(IndicatorEducationCatalog.knownTypes())
                .as("catalog entry for %s", type)
                .contains(type);
            IndicatorEducationCatalog.EducationRef ref = IndicatorEducationCatalog.forType(type);
            assertThat(ref.principle()).isNotNull();
            assertThat(ref.microlessonSlug())
                .as("lesson for %s", type)
                .startsWith("micro-principio-");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNKNOWN_TYPE", "MALWARE", ""})
    void unknownTypesFallBackToNeutralReference(String type) {
        IndicatorEducationCatalog.EducationRef ref = IndicatorEducationCatalog.forType(type);
        assertThat(ref.principle()).isEqualTo(PersuasionPrinciple.NONE);
        assertThat(ref.microlessonSlug()).isNotNull();
    }

    @Test
    void brandImpersonationMapsToAuthorityLesson() {
        IndicatorEducationCatalog.EducationRef ref =
            IndicatorEducationCatalog.forType("BRAND_IMPERSONATION");
        assertThat(ref.principle()).isEqualTo(PersuasionPrinciple.AUTHORITY);
        assertThat(ref.microlessonSlug()).isEqualTo("micro-principio-autoridad");
    }
}

package com.phishware.security;

import com.phishware.entity.enums.RiskLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NistThreatClassifier - Heurística léxica de URLs")
class NistThreatClassifierTest {

    private final NistThreatClassifier classifier = new NistThreatClassifier();

    private NistThreatClassifier.HeuristicResult classify(String url) {
        return classifier.classify(url, URI.create(url).getHost());
    }

    private boolean hasIndicator(NistThreatClassifier.HeuristicResult r, String type) {
        return r.indicators().stream().anyMatch(i -> i.type().equals(type));
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
        "https://paypa1.com/signin,            TYPOSQUATTING",
        "https://rnicrosoft-support.com,       TYPOSQUATTING",
        "https://bancolombla.com.co/personas,  TYPOSQUATTING",
        "https://paypal-seguridad.xyz,         BRAND_SQUATTING",
        "https://nequi-app.top/login,          BRAND_SQUATTING",
        "https://xn--pple-43d.com,             PUNYCODE_HOMOGRAPH",
        "https://www.google.com@evil.example,  USERINFO_TRICK",
        "http://example.com:8081/a,            NON_STANDARD_PORT",
        "https://good.com/r?url=https://evil.com, EMBEDDED_REDIRECT",
        "https://micuenta.000webhostapp.com,   FREE_HOSTING",
        "https://xk2j9qv7zt4m1pw.com,          RANDOM_LOOKING_DOMAIN"
    })
    void detectsIndicator(String url, String expectedType) {
        assertThat(hasIndicator(classify(url.trim()), expectedType.trim())).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "https://www.paypal.com/signin",
        "https://accounts.google.com/ServiceLogin",
        "https://www.bancolombia.com/personas",
        "https://github.com/spring-projects",
        "https://www.eltiempo.com/tecnologia"
    })
    void legitimateSitesAreSafe(String url) {
        NistThreatClassifier.HeuristicResult r = classify(url);
        assertThat(r.riskLevel()).as(r.indicators().toString()).isEqualTo(RiskLevel.SAFE);
    }

    @Test
    @DisplayName("Un dominio que solo termina en google.com no se considera oficial")
    void suffixWithoutDotIsNotOfficial() {
        assertThat(hasIndicator(classify("https://evilgoogle.com/login"), "BRAND_SQUATTING")).isTrue();
    }

    @Test
    @DisplayName("El TLD se evalúa sobre el host, no sobre la ruta")
    void tldIsCheckedOnHostOnly() {
        assertThat(hasIndicator(classify("https://example.com/blog.top/post"), "HIGH_RISK_TLD")).isFalse();
    }

    @Test
    void combinedSignalsAreDangerous() {
        assertThat(classify("http://secure-login.paypa1-verify.tk/account").riskLevel())
            .isEqualTo(RiskLevel.DANGEROUS);
    }
}

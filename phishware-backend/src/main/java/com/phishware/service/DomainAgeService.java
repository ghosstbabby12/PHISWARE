package com.phishware.service;

import com.phishware.entity.enums.Severity;
import com.phishware.security.BrandCatalog;
import com.phishware.security.DomainUtils;
import com.phishware.security.NistThreatClassifier.NistFunction;
import com.phishware.security.NistThreatClassifier.ThreatIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Antigüedad del dominio vía RDAP (sucesor estándar de WHOIS, RFC 9083). No requiere API key.
 * NIST ID.RA-2: la mayoría de dominios de phishing se usan en sus primeros días de vida.
 */
@Service
@Slf4j
public class DomainAgeService {

    private static final int MAX_CACHE = 2000;

    private final WebClient rdapClient;
    private final Map<String, DomainAgeResult> cache = new ConcurrentHashMap<>();

    public DomainAgeService(@Qualifier("rdapClient") WebClient rdapClient) {
        this.rdapClient = rdapClient;
    }

    public DomainAgeResult check(String host) {
        String domain = DomainUtils.registrableDomain(host);
        if (domain.isEmpty() || DomainUtils.isIpAddress(domain) || BrandCatalog.isAnyOfficialDomain(domain)) {
            return DomainAgeResult.unavailable();
        }
        DomainAgeResult cached = cache.get(domain);
        if (cached != null) return cached;

        DomainAgeResult result = query(domain);
        if (cache.size() >= MAX_CACHE) cache.clear();
        if (result.available()) cache.put(domain, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private DomainAgeResult query(String domain) {
        try {
            Map<String, Object> response = rdapClient.get()
                .uri("/domain/{domain}", domain)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(6));

            if (response == null || !(response.get("events") instanceof List<?> events)) {
                return DomainAgeResult.unavailable();
            }
            for (Object e : events) {
                Map<String, Object> event = (Map<String, Object>) e;
                if ("registration".equals(event.get("eventAction")) && event.get("eventDate") != null) {
                    LocalDate registered = OffsetDateTime.parse(event.get("eventDate").toString()).toLocalDate();
                    return DomainAgeResult.of(registered, ChronoUnit.DAYS.between(registered, LocalDate.now()));
                }
            }
        } catch (Exception e) {
            // Muchos ccTLD no publican RDAP; no es un error del sistema
            log.debug("RDAP no disponible para {}: {}", domain, e.getMessage());
        }
        return DomainAgeResult.unavailable();
    }

    public record DomainAgeResult(
        boolean available,
        LocalDate registeredOn,
        Long ageDays,
        double score,
        List<ThreatIndicator> indicators
    ) {
        static DomainAgeResult unavailable() {
            return new DomainAgeResult(false, null, null, 0.0, List.of());
        }

        static DomainAgeResult of(LocalDate registered, long days) {
            ThreatIndicator indicator = null;
            if (days < 7) {
                indicator = new ThreatIndicator("VERY_NEW_DOMAIN",
                    "El dominio fue registrado hace " + days + " días", Severity.HIGH, 60.0, NistFunction.IDENTIFY);
            } else if (days < 30) {
                indicator = new ThreatIndicator("NEW_DOMAIN",
                    "El dominio fue registrado hace " + days + " días (menos de un mes)",
                    Severity.HIGH, 45.0, NistFunction.IDENTIFY);
            } else if (days < 180) {
                indicator = new ThreatIndicator("RECENT_DOMAIN",
                    "El dominio tiene menos de 6 meses de antigüedad (" + days + " días)",
                    Severity.MEDIUM, 20.0, NistFunction.IDENTIFY);
            }
            List<ThreatIndicator> list = indicator == null ? List.of() : List.of(indicator);
            return new DomainAgeResult(true, registered, days,
                indicator == null ? 0.0 : indicator.scoreContribution(), list);
        }
    }
}

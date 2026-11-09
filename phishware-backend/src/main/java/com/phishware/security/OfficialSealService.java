package com.phishware.security;

import com.phishware.dto.response.OfficialSeal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Official-site seal: confirms when a domain belongs to a brand in the catalog.
 * NIST ID.RA-2 — authoritative allow-list used as a trust anchor; an official
 * domain never triggers brand-impersonation indicators.
 */
@Service
@Slf4j
public class OfficialSealService {

    public OfficialSeal check(String host) {
        if (host == null || host.isBlank()) {
            return OfficialSeal.none();
        }
        String normalized = host.toLowerCase().trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            return OfficialSeal.none();
        }
        for (BrandCatalog.Brand brand : BrandCatalog.BRANDS) {
            for (String official : brand.officialDomains()) {
                if (DomainUtils.isSameOrSubdomain(normalized, official)) {
                    return OfficialSeal.of(brand.name(), official);
                }
            }
        }
        return OfficialSeal.none();
    }
}

package com.phishware.security;

import com.phishware.dto.response.OfficialSeal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class OfficialSealServiceTest {

    private final OfficialSealService service = new OfficialSealService();

    @ParameterizedTest
    @ValueSource(strings = {"nequi.com.co", "www.nequi.com.co", "bancolombia.com", "dian.gov.co"})
    void officialDomainsGetSeal(String host) {
        OfficialSeal seal = service.check(host);
        assertThat(seal.official()).isTrue();
        assertThat(seal.brand()).isNotBlank();
        assertThat(seal.officialDomain()).isNotBlank();
    }

    @ParameterizedTest
    @ValueSource(strings = {"nequi-verificacion.tk", "paypal-secure-login.xyz", "example.com", "192.0.2.1"})
    void nonOfficialHostsGetNoSeal(String host) {
        assertThat(service.check(host)).isEqualTo(OfficialSeal.none());
    }

    @Test
    void blankHostGetsNoSeal() {
        assertThat(service.check(null)).isEqualTo(OfficialSeal.none());
        assertThat(service.check("  ")).isEqualTo(OfficialSeal.none());
    }
}

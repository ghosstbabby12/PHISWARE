package com.phishware.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwaspComplianceValidatorTest {

    private final OwaspComplianceValidator validator = new OwaspComplianceValidator();

    @ParameterizedTest
    @ValueSource(strings = {
        "localhost",
        "127.0.0.1",
        "2130706433",
        "0x7f000001",
        "0177.0.0.1",
        "127.1",
        "10.0.0.5",
        "192.168.0.10",
        "169.254.169.254",
        "0.0.0.0"
    })
    void loopbackAndPrivateHostsAreRejected(String host) {
        assertThatThrownBy(() -> validator.validateAgainstSsrf("http://" + host + "/", host))
            .isInstanceOf(SecurityException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"[::1]", "[::ffff:127.0.0.1]"})
    void ipv6LoopbackHostsAreRejected(String host) {
        assertThatThrownBy(() -> validator.validateAgainstSsrf("http://" + host + "/", host))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    void publicLiteralIpPassesWithoutDns() {
        assertThatNoException().isThrownBy(
            () -> validator.validateAgainstSsrf("http://93.184.216.34/", "93.184.216.34"));
    }

    @Test
    void nullDomainIsIgnored() {
        assertThatNoException().isThrownBy(() -> validator.validateAgainstSsrf("http://x/", null));
    }
}

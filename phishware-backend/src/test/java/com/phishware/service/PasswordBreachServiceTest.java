package com.phishware.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordBreachServiceTest {

    private final PasswordBreachService service =
        new PasswordBreachService(WebClient.builder().build());

    @Test
    void prefixIsNormalizedToUppercase() {
        assertThat(PasswordBreachService.normalizePrefix("a94a8")).isEqualTo("A94A8");
    }

    @Test
    void invalidPrefixesAreRejected() {
        assertThatThrownBy(() -> PasswordBreachService.normalizePrefix("xyz"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PasswordBreachService.normalizePrefix("A94A8BB"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PasswordBreachService.normalizePrefix(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rangeBodyIsParsedToSuffixCounts() {
        String body = "0018A45C4D1DEF81644B54AB7F969B88D65:3\r\n"
            + "00D4F6E8FA6EECAD2A3AA733ADA7CF7119D:10\r\n"
            + "MALFORMED\r\n";
        Map<String, Integer> suffixes = PasswordBreachService.parseRangeBody(body);
        assertThat(suffixes).containsEntry("0018A45C4D1DEF81644B54AB7F969B88D65", 3);
        assertThat(suffixes).containsEntry("00D4F6E8FA6EECAD2A3AA733ADA7CF7119D", 10);
        assertThat(suffixes).hasSize(2);
        assertThat(PasswordBreachService.parseRangeBody(null)).isEmpty();
    }
}

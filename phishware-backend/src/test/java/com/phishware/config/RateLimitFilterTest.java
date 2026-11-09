package com.phishware.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private int statusOf(RateLimitingConfig.RateLimitFilter filter, String remoteAddr,
                         String forwardedFor) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr(remoteAddr);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> res.getWriter().write("OK");
        filter.doFilter(request, response, chain);
        return response.getStatus();
    }

    @Test
    void spoofedForwardedHeaderIsIgnoredWithoutTrustedProxies() throws Exception {
        RateLimitingConfig.RateLimitFilter filter =
            new RateLimitingConfig.RateLimitFilter(5, List.of());
        // Same direct remote, rotating spoofed header: buckets must NOT split by header.
        for (int i = 0; i < 5; i++) {
            assertThat(statusOf(filter, "203.0.113.7", "10.9.9." + i)).isEqualTo(200);
        }
        assertThat(statusOf(filter, "203.0.113.7", "10.9.9.99")).isEqualTo(429);
    }

    @Test
    void forwardedHeaderIsHonoredFromTrustedProxy() throws Exception {
        RateLimitingConfig.RateLimitFilter filter =
            new RateLimitingConfig.RateLimitFilter(2, List.of("10.0.0.1"));
        // Same proxy, same client behind it: bucket shared by header value.
        assertThat(statusOf(filter, "10.0.0.1", "203.0.113.9")).isEqualTo(200);
        assertThat(statusOf(filter, "10.0.0.1", "203.0.113.9, 10.0.0.1")).isEqualTo(200);
        assertThat(statusOf(filter, "10.0.0.1", "203.0.113.9")).isEqualTo(429);
        // Different client behind the same proxy gets its own bucket.
        assertThat(statusOf(filter, "10.0.0.1", "203.0.113.10")).isEqualTo(200);
    }

    @Test
    void trustedCidrRangeIsSupported() {
        assertThat(RateLimitingConfig.RateLimitFilter.matchesCidr("10.0.0.0/8", "10.9.9.9")).isTrue();
        assertThat(RateLimitingConfig.RateLimitFilter.matchesCidr("10.0.0.0/8", "11.0.0.1")).isFalse();
        assertThat(RateLimitingConfig.RateLimitFilter.matchesCidr("not-a-cidr", "10.0.0.1")).isFalse();
    }
}

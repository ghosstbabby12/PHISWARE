package com.phishware.security;

import com.phishware.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(provider, "jwtSecret",
            "test-secret-key-that-is-long-enough-for-hs256-algorithm!!");
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 86400000L);
        ReflectionTestUtils.setField(provider, "refreshExpirationMs", 604800000L);
    }

    private User user() {
        return User.builder().id(1L).username("alice").email("alice@example.com").build();
    }

    @Test
    void accessTokenIsAcceptedButNotAsRefresh() {
        String access = provider.generateToken(user());

        assertThat(provider.validateToken(access)).isTrue();
        assertThat(provider.validateRefreshToken(access)).isFalse();
    }

    @Test
    void refreshTokenIsAcceptedOnlyAsRefresh() {
        String refresh = provider.generateRefreshToken(user());

        assertThat(provider.validateRefreshToken(refresh)).isTrue();
        assertThat(provider.validateToken(refresh)).isFalse();
        assertThat(provider.getUserIdFromToken(refresh)).isEqualTo(1L);
    }

    @Test
    void tamperedTokenIsRejected() {
        String refresh = provider.generateRefreshToken(user()) + "tampered";

        assertThat(provider.validateRefreshToken(refresh)).isFalse();
        assertThat(provider.validateToken(refresh)).isFalse();
    }
}

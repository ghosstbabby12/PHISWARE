package com.phishware.security;

import com.phishware.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        List<String> roles = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("userId", user.getId())
            .claim("email", user.getEmail())
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.warn("JWT token malformado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims vacíos: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}

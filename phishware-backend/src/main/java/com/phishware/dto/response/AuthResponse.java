package com.phishware.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
    private int points;
    private int level;
}

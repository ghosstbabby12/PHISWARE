package com.phishware.service;

import com.phishware.dto.request.LoginRequest;
import com.phishware.dto.request.RegisterRequest;
import com.phishware.dto.response.AuthResponse;
import com.phishware.entity.Role;
import com.phishware.entity.User;
import com.phishware.exception.ResourceNotFoundException;
import com.phishware.repository.RoleRepository;
import com.phishware.repository.UserRepository;
import com.phishware.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());
        auditLogService.log(user, "USER_LOGIN", "User", user.getId(), true, null, ipAddress);

        String token = tokenProvider.generateToken(user);
        return buildAuthResponse(user, token);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new ResourceNotFoundException("Rol USER no configurado"));

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .roles(Set.of(userRole))
            .build();

        user = userRepository.save(user);
        auditLogService.log(user, "USER_REGISTER", "User", user.getId(), true, null, ipAddress);

        String token = tokenProvider.generateToken(user);
        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(tokenProvider.getExpirationMs())
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .roles(user.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList()))
            .points(user.getPoints())
            .level(user.getLevel())
            .build();
    }
}

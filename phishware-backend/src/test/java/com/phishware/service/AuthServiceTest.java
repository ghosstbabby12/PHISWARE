package com.phishware.service;

import com.phishware.dto.request.RegisterRequest;
import com.phishware.dto.response.AuthResponse;
import com.phishware.entity.Role;
import com.phishware.entity.User;
import com.phishware.repository.RoleRepository;
import com.phishware.repository.UserRepository;
import com.phishware.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests unitarios")
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Registro exitoso con datos válidos")
    void register_WithValidData_ShouldReturnAuthResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("SecurePass@123");
        request.setFirstName("New");
        request.setLastName("User");

        Role userRole = Role.builder().id(1L).name("ROLE_USER").build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

        // Act
        AuthResponse response = authService.register(request, "127.0.0.1");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Registro debe fallar si el username ya existe")
    void register_WhenUsernameExists_ShouldThrowException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setEmail("new@example.com");
        request.setPassword("Pass@123");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request, "127.0.0.1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("nombre de usuario");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro debe fallar si el email ya existe")
    void register_WhenEmailExists_ShouldThrowException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("Pass@123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request, "127.0.0.1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("email");
    }

    @Test
    @DisplayName("Refresh válido rota el par de tokens")
    void refresh_WithValidToken_ShouldRotateTokenPair() {
        User user = User.builder().id(7L).username("rot").email("rot@example.com").build();

        when(tokenProvider.validateRefreshToken("refresh-ok")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("refresh-ok")).thenReturn(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(user)).thenReturn("access-new");
        when(tokenProvider.generateRefreshToken(user)).thenReturn("refresh-new");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);
        when(tokenProvider.getRefreshExpirationMs()).thenReturn(604800000L);

        AuthResponse response = authService.refresh("refresh-ok", "127.0.0.1");

        assertThat(response.getAccessToken()).isEqualTo("access-new");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-new");
        assertThat(response.getRefreshExpiresIn()).isEqualTo(604800000L);
        verify(auditLogService).log(eq(user), eq("USER_TOKEN_REFRESH"), eq("User"),
            eq(7L), eq(true), isNull(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Refresh inválido debe fallar sin tocar la base de datos")
    void refresh_WithInvalidToken_ShouldThrowException() {
        when(tokenProvider.validateRefreshToken("refresh-bad")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("refresh-bad", "127.0.0.1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Refresh token");

        verify(userRepository, never()).findById(any());
    }
}

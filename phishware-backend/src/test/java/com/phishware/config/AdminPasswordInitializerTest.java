package com.phishware.config;

import com.phishware.entity.User;
import com.phishware.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminPasswordInitializerTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private UserRepository repo;
    private User admin;
    private AdminPasswordInitializer init;

    @BeforeEach
    void setUp() {
        repo = mock(UserRepository.class);
        admin = new User();
        admin.setUsername("admin");
        admin.setPassword(AdminPasswordInitializer.LOCKED);
        when(repo.findByUsername("admin")).thenReturn(Optional.of(admin));
        init = new AdminPasswordInitializer(repo, encoder);
        ReflectionTestUtils.setField(init, "adminUsername", "admin");
    }

    private void run(String password) {
        ReflectionTestUtils.setField(init, "adminPassword", password);
        init.run(null);
    }

    @Test
    void sinVariableNoCambiaNada() {
        run("");
        assertThat(admin.getPassword()).isEqualTo(AdminPasswordInitializer.LOCKED);
        verify(repo, never()).save(any());
    }

    @Test
    void contrasenaCortaSeIgnora() {
        run("corta123");
        assertThat(admin.getPassword()).isEqualTo(AdminPasswordInitializer.LOCKED);
        verify(repo, never()).save(any());
    }

    @Test
    void aplicaLaContrasenaDelEntornoUnaSolaVez() {
        run("Una-Clave-Larga-2026!");
        assertThat(encoder.matches("Una-Clave-Larga-2026!", admin.getPassword())).isTrue();
        verify(repo, times(1)).save(admin);

        run("Una-Clave-Larga-2026!");   // segundo arranque: ya coincide, no reescribe
        verify(repo, times(1)).save(admin);
    }
}

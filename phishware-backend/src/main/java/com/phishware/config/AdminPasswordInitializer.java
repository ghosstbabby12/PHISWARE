package com.phishware.config;

import com.phishware.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fija la contraseña del admin desde la variable de entorno ADMIN_PASSWORD.
 * La contraseña nunca vive en el repositorio (OWASP A07 / NIST PR.AC-1):
 *  - ADMIN_PASSWORD vacía  → no se toca nada (si el admin está bloqueado por V10, se avisa).
 *  - ADMIN_PASSWORD < 12   → se ignora y se avisa (contraseña demasiado débil).
 *  - Si ya coincide        → no se reescribe en cada arranque.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminPasswordInitializer implements ApplicationRunner {

    static final int MIN_LENGTH = 12;
    static final String LOCKED = "!locked-seed-admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        userRepository.findByUsername(adminUsername).ifPresent(admin -> {
            if (adminPassword == null || adminPassword.isBlank()) {
                if (LOCKED.equals(admin.getPassword())) {
                    log.warn("[SECURITY] El admin '{}' está bloqueado (contraseña semilla). Define ADMIN_PASSWORD para activarlo.", adminUsername);
                }
                return;
            }
            if (adminPassword.length() < MIN_LENGTH) {
                log.warn("[SECURITY] ADMIN_PASSWORD ignorada: debe tener al menos {} caracteres.", MIN_LENGTH);
                return;
            }
            if (passwordEncoder.matches(adminPassword, admin.getPassword())) return;
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
            log.info("[SECURITY] Contraseña del admin '{}' actualizada desde ADMIN_PASSWORD.", adminUsername);
        });
    }
}

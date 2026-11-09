package com.phishware.controller;

import com.phishware.dto.request.PasswordPrefixRequest;
import com.phishware.service.PasswordBreachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/security")
@RequiredArgsConstructor
@Tag(name = "Seguridad", description = "Verificaciones de seguridad con k-anonimato")
public class SecurityController {

    private final PasswordBreachService breachService;

    @PostMapping("/password-check")
    @Operation(summary = "Verificar filtración de contraseña (k-anonimato)",
        description = "Recibe SOLO los primeros 5 caracteres hex del SHA-1 y devuelve los sufijos expuestos. La contraseña nunca sale del dispositivo.")
    public ResponseEntity<Map<String, Object>> passwordCheck(
            @Valid @RequestBody PasswordPrefixRequest request) {
        PasswordBreachService.RangeResult result = breachService.checkPrefix(request.getPrefix());
        return ResponseEntity.ok(Map.of(
            "prefix", result.prefix(),
            "exposedSuffixes", result.suffixes(),
            "exposedCount", result.suffixes().size(),
            "serviceAvailable", result.serviceAvailable()
        ));
    }
}

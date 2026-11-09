package com.phishware.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PasswordPrefixRequest {
    @NotBlank
    @Pattern(regexp = "[0-9A-Fa-f]{5}", message = "El prefijo debe ser exactamente 5 caracteres hexadecimales")
    private String prefix;
}

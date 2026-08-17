package com.phishware.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El username solo puede contener letras, números y guiones bajos")
    private String username;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-#])[A-Za-z\\d@$!%*?&_\\-#]{8,}$",
        message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String password;

    @Size(max = 80)
    private String firstName;

    @Size(max = 80)
    private String lastName;
}

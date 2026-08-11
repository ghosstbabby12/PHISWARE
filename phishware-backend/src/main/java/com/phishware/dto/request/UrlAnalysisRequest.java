package com.phishware.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UrlAnalysisRequest {

    @NotBlank(message = "La URL es requerida")
    @Size(max = 2048, message = "La URL no puede exceder 2048 caracteres")
    private String url;
}

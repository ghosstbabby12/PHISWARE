package com.phishware.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportRequest {

    @NotBlank(message = "La URL reportada es requerida")
    @Size(max = 2048, message = "La URL no puede exceder 2048 caracteres")
    private String reportedUrl;

    @NotBlank(message = "El tipo de reporte es requerido")
    @Pattern(regexp = "PHISHING|MALWARE|SUSPICIOUS|SCAM|SPAM",
             message = "Tipo de reporte debe ser: PHISHING, MALWARE, SUSPICIOUS, SCAM o SPAM")
    private String reportType;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @NotBlank(message = "La severidad es requerida")
    @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL",
             message = "Severidad debe ser: LOW, MEDIUM, HIGH o CRITICAL")
    private String severity;
}

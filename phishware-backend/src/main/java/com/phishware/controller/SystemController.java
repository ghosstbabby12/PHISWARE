package com.phishware.controller;

import com.phishware.service.GoogleSafeBrowsingService;
import com.phishware.service.VirusTotalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
@Tag(name = "Sistema", description = "Estado del sistema y configuración de APIs externas")
public class SystemController {

    private final GoogleSafeBrowsingService safeBrowsingService;
    private final VirusTotalService virusTotalService;

    @GetMapping("/api-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estado de APIs externas", description = "Indica qué APIs de inteligencia de amenazas están configuradas")
    public ResponseEntity<Map<String, Object>> apiStatus() {
        boolean gsbOk = safeBrowsingService.isConfigured();
        boolean vtOk  = virusTotalService.isConfigured();

        String mode = (gsbOk && vtOk)  ? "FULL"
                    : (gsbOk || vtOk)   ? "PARTIAL"
                    :                     "HEURISTIC_ONLY";

        return ResponseEntity.ok(Map.of(
            "googleSafeBrowsing", Map.of(
                "configured", gsbOk,
                "description", gsbOk ? "Activo — detección de malware y phishing Google" : "Sin configurar — solo heurística local"
            ),
            "virusTotal", Map.of(
                "configured", vtOk,
                "description", vtOk ? "Activo — 70+ motores antivirus" : "Sin configurar — solo heurística local"
            ),
            "analysisMode", mode,
            "heuristicWeight", (gsbOk && vtOk) ? "10%" : (gsbOk || vtOk) ? "30-40%" : "100%"
        ));
    }
}

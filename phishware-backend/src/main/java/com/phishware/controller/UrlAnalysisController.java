package com.phishware.controller;

import com.phishware.dto.request.UrlAnalysisRequest;
import com.phishware.dto.response.UrlAnalysisResponse;
import com.phishware.entity.User;
import com.phishware.entity.enums.RiskLevel;
import com.phishware.service.UrlAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Análisis de URLs", description = "Análisis y detección de phishing en URLs")
public class UrlAnalysisController {

    private final UrlAnalysisService analysisService;

    @PostMapping
    @Operation(summary = "Analizar URL", description = "Analiza una URL usando múltiples motores de seguridad")
    public ResponseEntity<UrlAnalysisResponse> analyzeUrl(
            @Valid @RequestBody UrlAnalysisRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(analysisService.analyzeUrl(request, currentUser.getId()));
    }

    @GetMapping("/history")
    @Operation(summary = "Historial de análisis", description = "Retorna el historial paginado de análisis del usuario")
    public ResponseEntity<Page<UrlAnalysisResponse>> getHistory(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Filtrar por nivel de riesgo: SAFE, SUSPICIOUS, DANGEROUS")
            @RequestParam(required = false) RiskLevel riskLevel,
            @PageableDefault(size = 10, sort = "analyzedAt") Pageable pageable) {
        return ResponseEntity.ok(analysisService.getUserHistory(currentUser.getId(), riskLevel, pageable));
    }
}

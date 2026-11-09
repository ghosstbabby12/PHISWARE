package com.phishware.controller;

import com.phishware.entity.enums.RiskLevel;
import com.phishware.repository.CommunityReportRepository;
import com.phishware.repository.UrlAnalysisRepository;
import com.phishware.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Administración", description = "Estadísticas globales del sistema")
public class AdminController {

    private final UrlAnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final CommunityReportRepository reportRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas globales", description = "Totales, distribución por nivel de riesgo y análisis por día (14 días)")
    public ResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> byRisk = new LinkedHashMap<>();
        byRisk.put("SAFE", analysisRepository.countByRiskLevel(RiskLevel.SAFE));
        byRisk.put("SUSPICIOUS", analysisRepository.countByRiskLevel(RiskLevel.SUSPICIOUS));
        byRisk.put("DANGEROUS", analysisRepository.countByRiskLevel(RiskLevel.DANGEROUS));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalUsers", userRepository.count());
        response.put("totalAnalyses", analysisRepository.count());
        response.put("phishingDetected", analysisRepository.countByIsPhishingTrue());
        response.put("pendingReports", reportRepository.countByStatus("PENDING"));
        response.put("byRiskLevel", byRisk);
        response.put("analysesPerDay", analysisRepository.countAnalysesPerDayLast14Days()
            .stream()
            .map(row -> Map.of("day", String.valueOf(row[0]), "count", ((Number) row[1]).longValue()))
            .toList());
        return ResponseEntity.ok(response);
    }
}

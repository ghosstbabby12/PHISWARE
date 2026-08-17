package com.phishware.controller;

import com.phishware.dto.request.ReportRequest;
import com.phishware.dto.response.ReportResponse;
import com.phishware.entity.User;
import com.phishware.service.CommunityReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Reportes Comunitarios", description = "Reportar URLs sospechosas y gestionar reportes")
public class CommunityReportController {

    private final CommunityReportService reportService;

    @PostMapping
    @Operation(summary = "Crear reporte", description = "Reportar una URL sospechosa a la comunidad (+10 pts)")
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(reportService.createReport(request, currentUser.getId()));
    }

    @GetMapping("/my-reports")
    @Operation(summary = "Mis reportes", description = "Historial paginado de reportes del usuario")
    public ResponseEntity<Page<ReportResponse>> getMyReports(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(reportService.getUserReports(currentUser.getId(), pageable));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reportes pendientes", description = "Listar reportes pendientes de revisión (solo admin)")
    public ResponseEntity<Page<ReportResponse>> getPendingReports(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(reportService.getPendingReports(pageable));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revisar reporte", description = "Cambiar estado de un reporte: CONFIRMED o REJECTED (solo admin)")
    public ResponseEntity<ReportResponse> reviewReport(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(reportService.reviewReport(id, status, currentUser.getId()));
    }
}

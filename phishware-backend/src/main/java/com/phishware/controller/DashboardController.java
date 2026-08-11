package com.phishware.controller;

import com.phishware.dto.response.DashboardResponse;
import com.phishware.entity.User;
import com.phishware.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Dashboard", description = "Estadísticas y métricas del usuario")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Dashboard del usuario", description = "Retorna estadísticas completas del usuario")
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(dashboardService.getUserDashboard(currentUser.getId()));
    }
}

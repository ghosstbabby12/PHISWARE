package com.phishware.controller;

import com.phishware.entity.Alert;
import com.phishware.entity.User;
import com.phishware.repository.AlertRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Alertas", description = "Gestión de alertas de seguridad del usuario")
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping
    @Operation(summary = "Listar alertas", description = "Retorna las alertas del usuario paginadas")
    public ResponseEntity<Page<Alert>> getAlerts(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(alertRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Contar no leídas", description = "Retorna el número de alertas no leídas")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User currentUser) {
        long count = alertRepository.countByUserIdAndIsReadFalse(currentUser.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar como leída", description = "Marca una alerta específica como leída")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        alertRepository.markAsRead(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marcar todas como leídas", description = "Marca todas las alertas del usuario como leídas")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User currentUser) {
        alertRepository.markAllAsReadByUserId(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}

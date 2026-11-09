package com.phishware.controller;

import com.phishware.dto.response.BadgeResponse;
import com.phishware.entity.User;
import com.phishware.service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gamification")
@RequiredArgsConstructor
@Tag(name = "Gamificación", description = "Insignias y rachas de actividad")
public class GamificationController {

    private final BadgeService badgeService;

    @GetMapping("/badges")
    @Operation(summary = "Listar insignias", description = "Las 12 insignias con estado de desbloqueo del usuario")
    public ResponseEntity<List<BadgeResponse>> badges(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(badgeService.listBadges(user.getId()));
    }

    @GetMapping("/streak")
    @Operation(summary = "Racha actual", description = "Días consecutivos con al menos un análisis")
    public ResponseEntity<Map<String, Object>> streak(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of(
            "currentStreakDays", badgeService.currentStreakDays(user.getId())
        ));
    }
}

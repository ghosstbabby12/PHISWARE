package com.phishware.service;

import com.phishware.dto.response.DashboardResponse;
import com.phishware.dto.response.UrlAnalysisResponse;
import com.phishware.entity.User;
import com.phishware.entity.enums.RiskLevel;
import com.phishware.repository.AlertRepository;
import com.phishware.repository.UrlAnalysisRepository;
import com.phishware.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UrlAnalysisRepository analysisRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final UrlAnalysisService analysisService;

    @Transactional(readOnly = true)
    public DashboardResponse getUserDashboard(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        long total     = analysisRepository.countByUserId(userId);
        long safe      = analysisRepository.countByUserIdAndRiskLevel(userId, RiskLevel.SAFE);
        long suspicious = analysisRepository.countByUserIdAndRiskLevel(userId, RiskLevel.SUSPICIOUS);
        long dangerous = analysisRepository.countByUserIdAndRiskLevel(userId, RiskLevel.DANGEROUS);
        long threats   = analysisRepository.countByUserIdAndIsPhishingTrue(userId);
        long unread    = alertRepository.countByUserIdAndIsReadFalse(userId);

        // Últimas 5 análisis
        List<UrlAnalysisResponse> recent = analysisService
            .getUserHistory(userId, null, PageRequest.of(0, 5))
            .getContent();

        // Análisis por día (últimos 7 días)
        Map<String, Long> byDay = new LinkedHashMap<>();
        analysisRepository.countAnalysesByDayForUser(userId)
            .forEach(row -> byDay.put((String) row[0], ((Number) row[1]).longValue()));

        // Amenazas por tipo
        Map<String, Long> threatsByType = new LinkedHashMap<>();
        analysisRepository.countThreatsByTypeForUser(userId)
            .forEach(row -> threatsByType.put((String) row[0], ((Number) row[1]).longValue()));

        return DashboardResponse.builder()
            .totalAnalyses(total)
            .threatsDetected(threats)
            .safeUrls(safe)
            .suspiciousUrls(suspicious)
            .dangerousUrls(dangerous)
            .unreadAlerts(unread)
            .userPoints(user.getPoints())
            .userLevel(user.getLevel())
            .recentAnalyses(recent)
            .analysesByDay(byDay)
            .threatsByType(threatsByType)
            .build();
    }
}

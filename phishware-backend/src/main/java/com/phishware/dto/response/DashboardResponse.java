package com.phishware.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {
    private long totalAnalyses;
    private long threatsDetected;
    private long safeUrls;
    private long suspiciousUrls;
    private long dangerousUrls;
    private long unreadAlerts;
    private int userPoints;
    private int userLevel;
    private List<UrlAnalysisResponse> recentAnalyses;
    private Map<String, Long> threatsByType;
    private Map<String, Long> analysesByDay;
}

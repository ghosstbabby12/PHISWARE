package com.phishware.dto.response;

import com.phishware.entity.enums.AnalysisSource;
import com.phishware.entity.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UrlAnalysisResponse {
    private Long id;
    private UUID uuid;
    private String originalUrl;
    private String domain;
    private RiskLevel riskLevel;
    private BigDecimal riskScore;
    private boolean isPhishing;
    private AnalysisSource analysisSource;
    private List<ThreatResponse> threats;
    private Integer analysisTimeMs;
    private LocalDateTime analyzedAt;
    private String riskMessage;
    private List<String> recommendations;
}

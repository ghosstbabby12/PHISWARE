package com.phishware.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BadgeResponse {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String badgeType;
    private int conditionValue;
    private int pointsReward;
    private boolean earned;
    private LocalDateTime earnedAt;
}

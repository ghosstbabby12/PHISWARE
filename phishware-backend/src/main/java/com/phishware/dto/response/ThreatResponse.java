package com.phishware.dto.response;

import com.phishware.entity.enums.Severity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ThreatResponse {
    private Long id;
    private String threatType;
    private String description;
    private Severity severity;
    private String source;
    private LocalDateTime detectedAt;
}

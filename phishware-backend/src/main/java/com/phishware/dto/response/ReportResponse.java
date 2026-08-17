package com.phishware.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private String reportedUrl;
    private String reportType;
    private String description;
    private String severity;
    private String status;
    private String reportedBy;
    private LocalDateTime createdAt;
}

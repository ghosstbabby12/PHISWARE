package com.phishware.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reported_url", nullable = false, length = 2048)
    private String reportedUrl;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

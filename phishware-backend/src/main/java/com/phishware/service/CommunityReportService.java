package com.phishware.service;

import com.phishware.dto.request.ReportRequest;
import com.phishware.dto.response.ReportResponse;
import com.phishware.entity.CommunityReport;
import com.phishware.entity.User;
import com.phishware.exception.ResourceNotFoundException;
import com.phishware.repository.CommunityReportRepository;
import com.phishware.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityReportService {

    private final CommunityReportRepository reportRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    @Transactional
    public ReportResponse createReport(ReportRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        CommunityReport report = CommunityReport.builder()
            .user(user)
            .reportedUrl(request.getReportedUrl())
            .reportType(request.getReportType())
            .description(request.getDescription())
            .severity(request.getSeverity())
            .status("PENDING")
            .build();

        report = reportRepository.save(report);

        gamificationService.onCommunityReport(user);

        log.info("Reporte comunitario creado: id={} user={} type={} url={}",
            report.getId(), user.getUsername(), request.getReportType(), request.getReportedUrl());

        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getUserReports(Long userId, Pageable pageable) {
        return reportRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ReportResponse> getPendingReports(Pageable pageable) {
        return reportRepository.findByStatusOrderByCreatedAtDesc("PENDING", pageable)
            .map(this::toResponse);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ReportResponse reviewReport(Long reportId, String newStatus, Long adminId) {
        CommunityReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado"));

        report.setStatus(newStatus);
        report.setReviewedBy(admin);
        report.setReviewedAt(java.time.LocalDateTime.now());
        report = reportRepository.save(report);

        log.info("Reporte revisado: id={} status={} admin={}", reportId, newStatus, admin.getUsername());

        return toResponse(report);
    }

    private ReportResponse toResponse(CommunityReport report) {
        return ReportResponse.builder()
            .id(report.getId())
            .reportedUrl(report.getReportedUrl())
            .reportType(report.getReportType())
            .description(report.getDescription())
            .severity(report.getSeverity())
            .status(report.getStatus())
            .reportedBy(report.getUser().getUsername())
            .createdAt(report.getCreatedAt())
            .build();
    }
}

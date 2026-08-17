package com.phishware.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.phishware.entity.CommunityReport;

public interface CommunityReportRepository extends JpaRepository<CommunityReport, Long> {

    Page<CommunityReport> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<CommunityReport> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    long countByUserId(Long userId);

    long countByStatus(String status);

    @Query("SELECT cr.reportType, COUNT(cr) FROM CommunityReport cr GROUP BY cr.reportType ORDER BY COUNT(cr) DESC")
    java.util.List<Object[]> countByReportType();
}

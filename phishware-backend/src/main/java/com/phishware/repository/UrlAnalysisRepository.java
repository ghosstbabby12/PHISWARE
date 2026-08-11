package com.phishware.repository;

import com.phishware.entity.UrlAnalysis;
import com.phishware.entity.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UrlAnalysisRepository extends JpaRepository<UrlAnalysis, Long> {

    Page<UrlAnalysis> findByUserIdOrderByAnalyzedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    long countByUserIdAndRiskLevel(Long userId, RiskLevel riskLevel);

    long countByUserIdAndIsPhishingTrue(Long userId);

    @Query("""
        SELECT u FROM UrlAnalysis u
        WHERE u.user.id = :userId
        AND (:riskLevel IS NULL OR u.riskLevel = :riskLevel)
        ORDER BY u.analyzedAt DESC
        """)
    Page<UrlAnalysis> findByUserIdWithFilters(
        @Param("userId") Long userId,
        @Param("riskLevel") RiskLevel riskLevel,
        Pageable pageable
    );

    @Query("""
        SELECT COUNT(u) FROM UrlAnalysis u
        WHERE u.user.id = :userId
        AND u.analyzedAt BETWEEN :start AND :end
        """)
    long countByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query(value = """
        SELECT TO_CHAR(analyzed_at, 'YYYY-MM-DD') as day, COUNT(*) as count
        FROM url_analysis
        WHERE user_id = :userId
        AND analyzed_at >= NOW() - INTERVAL '7 days'
        GROUP BY TO_CHAR(analyzed_at, 'YYYY-MM-DD')
        ORDER BY day
        """, nativeQuery = true)
    java.util.List<Object[]> countAnalysesByDayForUser(@Param("userId") Long userId);

    // Admin stats
    long countByRiskLevel(RiskLevel riskLevel);

    long countByIsPhishingTrue();
}

package com.phishware.repository;

import com.phishware.entity.EducationalContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EducationalContentRepository extends JpaRepository<EducationalContent, Long> {

    Optional<EducationalContent> findBySlugAndIsPublishedTrue(String slug);

    Page<EducationalContent> findByIsPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<EducationalContent> findByCategoryAndIsPublishedTrue(String category, Pageable pageable);

    @Query("""
        SELECT e FROM EducationalContent e
        WHERE e.isPublished = true
        AND (:category IS NULL OR e.category = :category)
        AND (:difficulty IS NULL OR e.difficulty = :difficulty)
        ORDER BY e.createdAt DESC
        """)
    Page<EducationalContent> findWithFilters(
        @Param("category") String category,
        @Param("difficulty") String difficulty,
        Pageable pageable
    );

    @Modifying
    @Query("UPDATE EducationalContent e SET e.views = e.views + 1 WHERE e.id = :id")
    void incrementViews(@Param("id") Long id);
}

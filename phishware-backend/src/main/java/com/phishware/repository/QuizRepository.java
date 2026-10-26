package com.phishware.repository;

import com.phishware.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions LEFT JOIN FETCH q.content WHERE q.id = :id AND q.isActive = true")
    Optional<Quiz> findActiveWithQuestions(@Param("id") Long id);

    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions JOIN FETCH q.content c " +
           "WHERE c.slug = :slug AND q.isActive = true")
    Optional<Quiz> findActiveByContentSlug(@Param("slug") String slug);

    /** Quizzes vinculados a un módulo publicado (uno por módulo). */
    @Query("SELECT DISTINCT q FROM Quiz q JOIN FETCH q.content c LEFT JOIN FETCH q.questions " +
           "WHERE q.isActive = true AND c.isPublished = true ORDER BY c.id")
    List<Quiz> findActiveModuleQuizzes();
}

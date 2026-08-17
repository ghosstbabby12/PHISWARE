package com.phishware.repository;

import com.phishware.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findByUserIdOrderByCompletedAtDesc(Long userId);

    boolean existsByUserIdAndQuizId(Long userId, Long quizId);

    @Query("SELECT COUNT(r) FROM QuizResult r WHERE r.user.id = :userId AND r.passed = true")
    long countPassedByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(r.score) FROM QuizResult r WHERE r.user.id = :userId")
    Double averageScoreByUserId(@Param("userId") Long userId);
}

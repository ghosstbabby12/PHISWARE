package com.phishware.repository;

import com.phishware.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
}

package com.phishware.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** Seguimiento del aprendizaje: estado del usuario en el quiz de cada módulo. */
@Data
@Builder
public class LearningProgressResponse {
    private int totalModules;
    private int modulesPassed;
    private int modulesAttempted;
    private int completionPercentage;
    private Integer averageBestScore;
    private int totalAttempts;
    private int quizPointsEarned;
    private List<ModuleProgress> modules;
    private List<AttemptSummary> recentAttempts;

    @Data
    @Builder
    public static class ModuleProgress {
        private Long contentId;
        private String slug;
        private String title;
        private String category;
        private String difficulty;
        private Long quizId;
        private String quizTitle;
        private int questionCount;
        private int passingScore;
        private int attempts;
        private Integer bestScore;
        private Integer lastScore;
        private boolean passed;
        private LocalDateTime lastAttemptAt;
        /** NOT_STARTED, IN_PROGRESS (intentado sin aprobar) o PASSED */
        private String status;
    }

    @Data
    @Builder
    public static class AttemptSummary {
        private Long id;
        private Long quizId;
        private String quizTitle;
        private String moduleSlug;
        private int score;
        private boolean passed;
        private int pointsEarned;
        private LocalDateTime completedAt;
    }
}

package com.phishware.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** Quiz listo para responder: nunca incluye las respuestas correctas. */
@Data
@Builder
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private Long contentId;
    private String contentSlug;
    private String contentTitle;
    private String difficulty;
    private int timeLimitSec;
    private int passingScore;
    private int pointsReward;
    private List<Question> questions;

    // Estado del usuario actual en este quiz
    private int attempts;
    private Integer bestScore;
    private boolean passed;

    @Data
    @Builder
    public static class Question {
        private Long id;
        private String questionText;
        private String questionType;
        private Map<String, String> options;
        private int points;
        private int orderIndex;
    }
}

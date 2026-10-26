package com.phishware.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuizResultResponse {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private int score;
    private boolean passed;
    private int passingScore;
    private int pointsEarned;
    private boolean firstTimePassed;
    private int correctCount;
    private int totalQuestions;
    private int attempts;
    private int bestScore;
    private Integer timeTakenSec;
    private LocalDateTime completedAt;
    private List<QuestionFeedback> feedback;

    @Data
    @Builder
    public static class QuestionFeedback {
        private Long questionId;
        private boolean correct;
        private List<String> selected;
        private List<String> correctAnswers;
        private String explanation;
    }
}

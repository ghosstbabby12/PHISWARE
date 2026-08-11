package com.phishware.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Map;

@Data
public class QuizAnswerRequest {

    @NotNull(message = "El ID del quiz es requerido")
    @Positive
    private Long quizId;

    @NotNull(message = "Las respuestas son requeridas")
    private Map<Long, Object> answers;

    @Positive
    private Integer timeTakenSec;
}

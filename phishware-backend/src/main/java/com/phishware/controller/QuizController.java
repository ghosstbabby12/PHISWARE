package com.phishware.controller;

import com.phishware.dto.request.QuizAnswerRequest;
import com.phishware.dto.response.LearningProgressResponse;
import com.phishware.dto.response.QuizResponse;
import com.phishware.dto.response.QuizResultResponse;
import com.phishware.entity.User;
import com.phishware.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Quizzes", description = "Evaluación por módulo y seguimiento del aprendizaje")
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/progress")
    @Operation(summary = "Progreso de aprendizaje", description = "Estado del usuario en el quiz de cada módulo educativo")
    public ResponseEntity<LearningProgressResponse> getProgress(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(quizService.getProgress(currentUser.getId()));
    }

    @GetMapping("/module/{slug}")
    @Operation(summary = "Quiz de un módulo", description = "Retorna el quiz asociado a un artículo (sin respuestas correctas)")
    public ResponseEntity<QuizResponse> getByModule(
            @PathVariable String slug,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(quizService.getQuizForModule(slug, currentUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener quiz", description = "Retorna un quiz por id (sin respuestas correctas)")
    public ResponseEntity<QuizResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(quizService.getQuiz(id, currentUser.getId()));
    }

    @PostMapping("/submit")
    @Operation(summary = "Enviar respuestas", description = "Califica el quiz, guarda el intento y retorna la retroalimentación")
    public ResponseEntity<QuizResultResponse> submit(
            @Valid @RequestBody QuizAnswerRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(quizService.submit(request, currentUser.getId()));
    }
}

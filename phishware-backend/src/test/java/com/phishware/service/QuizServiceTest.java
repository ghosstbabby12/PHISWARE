package com.phishware.service;

import com.phishware.dto.request.QuizAnswerRequest;
import com.phishware.dto.response.LearningProgressResponse;
import com.phishware.dto.response.QuizResponse;
import com.phishware.dto.response.QuizResultResponse;
import com.phishware.entity.EducationalContent;
import com.phishware.entity.Quiz;
import com.phishware.entity.QuizQuestion;
import com.phishware.entity.QuizResult;
import com.phishware.entity.User;
import com.phishware.repository.QuizRepository;
import com.phishware.repository.QuizResultRepository;
import com.phishware.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizService - Calificación y seguimiento")
class QuizServiceTest {

    @Mock private QuizRepository quizRepository;
    @Mock private QuizResultRepository resultRepository;
    @Mock private UserRepository userRepository;
    @Mock private GamificationService gamificationService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private QuizService quizService;

    private Quiz quiz;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("u").email("u@x.com").password("p").points(0).level(1).build();
        EducationalContent content = new EducationalContent();
        content.setId(3L);
        content.setSlug("smishing-phishing-por-sms");
        content.setTitle("Smishing");
        content.setCategory("SMISHING");
        quiz = Quiz.builder().id(10L).title("Quiz Smishing").difficulty("BEGINNER")
            .passingScore(70).pointsReward(50).content(content).build();
        quiz.setQuestions(List.of(
            question(100L, 1, "SINGLE_CHOICE", List.of("B")),
            question(101L, 2, "MULTIPLE_CHOICE", List.of("A", "B", "D")),
            question(102L, 3, "TRUE_FALSE", List.of("B"))
        ));
        lenient().when(quizRepository.findActiveWithQuestions(10L)).thenReturn(Optional.of(quiz));
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        lenient().when(resultRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private QuizQuestion question(Long id, int order, String type, List<String> correct) {
        return QuizQuestion.builder().id(id).quiz(quiz).questionText("Q" + id).questionType(type)
            .options(Map.of("B", "b", "A", "a", "C", "c", "D", "d"))
            .correctAnswers(Map.of("correct", correct)).explanation("porque sí").points(10).orderIndex(order).build();
    }

    private QuizAnswerRequest request(Map<Long, Object> answers) {
        QuizAnswerRequest r = new QuizAnswerRequest();
        r.setQuizId(10L);
        r.setAnswers(answers);
        r.setTimeTakenSec(60);
        return r;
    }

    @Test
    @DisplayName("Todas correctas: 100%, aprueba y otorga puntos la primera vez")
    void allCorrect_FirstPass_AwardsPoints() {
        when(resultRepository.existsByUserIdAndQuizIdAndPassedTrue(1L, 10L)).thenReturn(false);

        QuizResultResponse r = quizService.submit(
            request(Map.of(100L, "b", 101L, List.of("D", "A", "B"), 102L, "B")), 1L);

        assertThat(r.getScore()).isEqualTo(100);
        assertThat(r.isPassed()).isTrue();
        assertThat(r.isFirstTimePassed()).isTrue();
        assertThat(r.getPointsEarned()).isEqualTo(50);
        assertThat(r.getCorrectCount()).isEqualTo(3);
        verify(gamificationService).onQuizCompleted(user, 50);
    }

    @Test
    @DisplayName("Selección múltiple incompleta cuenta como incorrecta")
    void partialMultipleChoice_IsWrong() {
        QuizResultResponse r = quizService.submit(
            request(Map.of(100L, "B", 101L, List.of("A", "B"), 102L, "B")), 1L);

        assertThat(r.getScore()).isEqualTo(67);
        assertThat(r.isPassed()).isFalse();
        assertThat(r.getFeedback().get(1).isCorrect()).isFalse();
        assertThat(r.getFeedback().get(1).getCorrectAnswers()).containsExactly("A", "B", "D");
        verify(gamificationService, never()).onQuizCompleted(any(), anyInt());
    }

    @Test
    @DisplayName("Aprobar de nuevo no vuelve a otorgar puntos")
    void secondPass_NoPoints() {
        when(resultRepository.existsByUserIdAndQuizIdAndPassedTrue(1L, 10L)).thenReturn(true);

        QuizResultResponse r = quizService.submit(
            request(Map.of(100L, "B", 101L, List.of("A", "B", "D"), 102L, "B")), 1L);

        assertThat(r.isPassed()).isTrue();
        assertThat(r.getPointsEarned()).isZero();
        verify(gamificationService, never()).onQuizCompleted(any(), anyInt());
    }

    @Test
    @DisplayName("Preguntas sin responder cuentan como incorrectas")
    void unanswered_AreWrong() {
        Map<Long, Object> answers = new HashMap<>();
        answers.put(100L, null);
        QuizResultResponse r = quizService.submit(request(answers), 1L);
        assertThat(r.getScore()).isZero();
    }

    @Test
    @DisplayName("El quiz enviado al navegador no expone las respuestas y ordena las opciones")
    void quizResponse_HidesAnswers() {
        QuizResponse r = quizService.getQuiz(10L, 1L);
        assertThat(r.getQuestions()).hasSize(3);
        assertThat(r.getQuestions().get(0).getOptions().keySet()).containsExactly("A", "B", "C", "D");
        assertThat(r.toString()).doesNotContain("correct");
    }

    @Test
    @DisplayName("El progreso marca cada módulo como aprobado, en progreso o sin empezar")
    void progress_ComputesStatus() {
        Quiz other = Quiz.builder().id(11L).title("Otro").difficulty("BEGINNER").passingScore(70)
            .content(quiz.getContent()).build();
        when(quizRepository.findActiveModuleQuizzes()).thenReturn(List.of(quiz, other));
        when(resultRepository.findByUserIdOrderByCompletedAtDesc(1L)).thenReturn(List.of(
            QuizResult.builder().id(2L).quiz(quiz).user(user).score(80).passed(true).pointsEarned(50).build(),
            QuizResult.builder().id(1L).quiz(quiz).user(user).score(40).passed(false).build()));

        LearningProgressResponse p = quizService.getProgress(1L);

        assertThat(p.getTotalModules()).isEqualTo(2);
        assertThat(p.getModulesPassed()).isEqualTo(1);
        assertThat(p.getCompletionPercentage()).isEqualTo(50);
        assertThat(p.getModules().get(0).getStatus()).isEqualTo("PASSED");
        assertThat(p.getModules().get(0).getBestScore()).isEqualTo(80);
        assertThat(p.getModules().get(0).getAttempts()).isEqualTo(2);
        assertThat(p.getModules().get(1).getStatus()).isEqualTo("NOT_STARTED");
        assertThat(p.getQuizPointsEarned()).isEqualTo(50);
    }
}

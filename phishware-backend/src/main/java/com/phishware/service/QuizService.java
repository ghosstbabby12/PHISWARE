package com.phishware.service;

import com.phishware.dto.request.QuizAnswerRequest;
import com.phishware.dto.response.LearningProgressResponse;
import com.phishware.dto.response.QuizResponse;
import com.phishware.dto.response.QuizResultResponse;
import com.phishware.entity.Quiz;
import com.phishware.entity.QuizQuestion;
import com.phishware.entity.QuizResult;
import com.phishware.entity.User;
import com.phishware.exception.ResourceNotFoundException;
import com.phishware.repository.QuizRepository;
import com.phishware.repository.QuizResultRepository;
import com.phishware.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Quizzes por módulo educativo — NIST CSF RC.IM-1 / PR.AT-1: concienciación y formación.
 *
 * La calificación se hace siempre en el servidor: las respuestas correctas nunca se
 * envían al navegador antes de que el usuario responda.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository       quizRepository;
    private final QuizResultRepository resultRepository;
    private final UserRepository       userRepository;
    private final GamificationService  gamificationService;
    private final AuditLogService      auditLogService;

    // ── Consulta de quizzes ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public QuizResponse getQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findActiveWithQuestions(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz no encontrado: " + quizId));
        return toQuizResponse(quiz, userId);
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizForModule(String slug, Long userId) {
        Quiz quiz = quizRepository.findActiveByContentSlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Este módulo aún no tiene quiz"));
        return toQuizResponse(quiz, userId);
    }

    // ── Envío y calificación ──────────────────────────────────────────────────

    @Transactional
    public QuizResultResponse submit(QuizAnswerRequest request, Long userId) {
        Quiz quiz = quizRepository.findActiveWithQuestions(request.getQuizId())
            .orElseThrow(() -> new ResourceNotFoundException("Quiz no encontrado: " + request.getQuizId()));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<QuizQuestion> questions = sortedQuestions(quiz);
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("El quiz no tiene preguntas");
        }

        Map<Long, Object> answers = request.getAnswers() == null ? Map.of() : request.getAnswers();
        int totalPoints = 0, earnedPoints = 0, correctCount = 0;
        List<QuizResultResponse.QuestionFeedback> feedback = new ArrayList<>();
        Map<String, Object> storedAnswers = new LinkedHashMap<>();

        for (QuizQuestion question : questions) {
            Set<String> selected = normalizeAnswer(answers.get(question.getId()));
            Set<String> correct  = correctAnswersOf(question);
            boolean isCorrect = !selected.isEmpty() && selected.equals(correct);

            totalPoints += question.getPoints();
            if (isCorrect) {
                earnedPoints += question.getPoints();
                correctCount++;
            }
            storedAnswers.put(String.valueOf(question.getId()), List.copyOf(selected));
            feedback.add(QuizResultResponse.QuestionFeedback.builder()
                .questionId(question.getId())
                .correct(isCorrect)
                .selected(List.copyOf(selected))
                .correctAnswers(List.copyOf(correct))
                .explanation(question.getExplanation())
                .build());
        }

        int score = totalPoints == 0 ? 0 : Math.round(earnedPoints * 100f / totalPoints);
        boolean passed = score >= quiz.getPassingScore();

        // Los puntos solo se otorgan la primera vez que se aprueba, para que repetir no sume infinito
        boolean alreadyPassed = resultRepository.existsByUserIdAndQuizIdAndPassedTrue(userId, quiz.getId());
        boolean firstTimePassed = passed && !alreadyPassed;
        int pointsEarned = firstTimePassed ? quiz.getPointsReward() : 0;

        QuizResult result = resultRepository.save(QuizResult.builder()
            .user(user)
            .quiz(quiz)
            .score(score)
            .passed(passed)
            .pointsEarned(pointsEarned)
            .timeTakenSec(request.getTimeTakenSec())
            .answers(storedAnswers)
            .completedAt(LocalDateTime.now())
            .build());

        if (pointsEarned > 0) {
            gamificationService.onQuizCompleted(user, pointsEarned);
        }
        auditLogService.log(user, "QUIZ_COMPLETED", "Quiz", quiz.getId(), true, null, null);

        List<QuizResult> attempts = resultRepository.findByUserIdAndQuizIdOrderByCompletedAtDesc(userId, quiz.getId());
        int bestScore = attempts.stream().mapToInt(QuizResult::getScore).max().orElse(score);

        return QuizResultResponse.builder()
            .id(result.getId())
            .quizId(quiz.getId())
            .quizTitle(quiz.getTitle())
            .score(score)
            .passed(passed)
            .passingScore(quiz.getPassingScore())
            .pointsEarned(pointsEarned)
            .firstTimePassed(firstTimePassed)
            .correctCount(correctCount)
            .totalQuestions(questions.size())
            .attempts(attempts.size())
            .bestScore(bestScore)
            .timeTakenSec(request.getTimeTakenSec())
            .completedAt(result.getCompletedAt())
            .feedback(feedback)
            .build();
    }

    // ── Seguimiento del aprendizaje ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public LearningProgressResponse getProgress(Long userId) {
        List<Quiz> quizzes = quizRepository.findActiveModuleQuizzes();
        List<QuizResult> results = resultRepository.findByUserIdOrderByCompletedAtDesc(userId);
        Map<Long, List<QuizResult>> byQuiz = results.stream()
            .collect(Collectors.groupingBy(r -> r.getQuiz().getId()));

        List<LearningProgressResponse.ModuleProgress> modules = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            List<QuizResult> attempts = byQuiz.getOrDefault(quiz.getId(), List.of());
            boolean passed = attempts.stream().anyMatch(QuizResult::isPassed);
            Integer best = attempts.stream().map(QuizResult::getScore).max(Integer::compare).orElse(null);
            QuizResult last = attempts.isEmpty() ? null : attempts.get(0);   // ya vienen ordenados desc

            modules.add(LearningProgressResponse.ModuleProgress.builder()
                .contentId(quiz.getContent().getId())
                .slug(quiz.getContent().getSlug())
                .title(quiz.getContent().getTitle())
                .category(quiz.getContent().getCategory())
                .difficulty(quiz.getDifficulty())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .questionCount(quiz.getQuestions().size())
                .passingScore(quiz.getPassingScore())
                .attempts(attempts.size())
                .bestScore(best)
                .lastScore(last == null ? null : last.getScore())
                .lastAttemptAt(last == null ? null : last.getCompletedAt())
                .passed(passed)
                .status(passed ? "PASSED" : attempts.isEmpty() ? "NOT_STARTED" : "IN_PROGRESS")
                .build());
        }

        int passedCount    = (int) modules.stream().filter(LearningProgressResponse.ModuleProgress::isPassed).count();
        int attemptedCount = (int) modules.stream().filter(m -> m.getAttempts() > 0).count();
        Integer avgBest = attemptedCount == 0 ? null : (int) Math.round(modules.stream()
            .filter(m -> m.getBestScore() != null)
            .mapToInt(LearningProgressResponse.ModuleProgress::getBestScore)
            .average().orElse(0));

        Map<Long, Quiz> quizById = quizzes.stream().collect(Collectors.toMap(Quiz::getId, q -> q));
        List<LearningProgressResponse.AttemptSummary> recent = results.stream()
            .limit(10)
            .map(r -> {
                Quiz q = quizById.get(r.getQuiz().getId());
                return LearningProgressResponse.AttemptSummary.builder()
                    .id(r.getId())
                    .quizId(r.getQuiz().getId())
                    .quizTitle(q != null ? q.getTitle() : r.getQuiz().getTitle())
                    .moduleSlug(q != null ? q.getContent().getSlug() : null)
                    .score(r.getScore())
                    .passed(r.isPassed())
                    .pointsEarned(r.getPointsEarned())
                    .completedAt(r.getCompletedAt())
                    .build();
            })
            .toList();

        return LearningProgressResponse.builder()
            .totalModules(modules.size())
            .modulesPassed(passedCount)
            .modulesAttempted(attemptedCount)
            .completionPercentage(modules.isEmpty() ? 0 : Math.round(passedCount * 100f / modules.size()))
            .averageBestScore(avgBest)
            .totalAttempts(results.size())
            .quizPointsEarned(results.stream().mapToInt(QuizResult::getPointsEarned).sum())
            .modules(modules)
            .recentAttempts(recent)
            .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private QuizResponse toQuizResponse(Quiz quiz, Long userId) {
        List<QuizResult> attempts = resultRepository.findByUserIdAndQuizIdOrderByCompletedAtDesc(userId, quiz.getId());
        List<QuizResponse.Question> questions = sortedQuestions(quiz).stream()
            .map(q -> QuizResponse.Question.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .questionType(q.getQuestionType())
                .options(new LinkedHashMap<>(new java.util.TreeMap<>(q.getOptions())))
                .points(q.getPoints())
                .orderIndex(q.getOrderIndex())
                .build())
            .toList();

        return QuizResponse.builder()
            .id(quiz.getId())
            .title(quiz.getTitle())
            .description(quiz.getDescription())
            .contentId(quiz.getContent() != null ? quiz.getContent().getId() : null)
            .contentSlug(quiz.getContent() != null ? quiz.getContent().getSlug() : null)
            .contentTitle(quiz.getContent() != null ? quiz.getContent().getTitle() : null)
            .difficulty(quiz.getDifficulty())
            .timeLimitSec(quiz.getTimeLimitSec())
            .passingScore(quiz.getPassingScore())
            .pointsReward(quiz.getPointsReward())
            .questions(questions)
            .attempts(attempts.size())
            .bestScore(attempts.stream().map(QuizResult::getScore).max(Integer::compare).orElse(null))
            .passed(attempts.stream().anyMatch(QuizResult::isPassed))
            .build();
    }

    private static List<QuizQuestion> sortedQuestions(Quiz quiz) {
        return quiz.getQuestions().stream()
            .sorted(Comparator.comparingInt(QuizQuestion::getOrderIndex))
            .toList();
    }

    /** Acepta "B", ["A","C"] o null y lo convierte en un conjunto ordenado de letras. */
    static Set<String> normalizeAnswer(Object raw) {
        Set<String> result = new TreeSet<>();
        if (raw instanceof Collection<?> list) {
            list.forEach(v -> { if (v != null && !v.toString().isBlank()) result.add(v.toString().trim().toUpperCase()); });
        } else if (raw != null && !raw.toString().isBlank()) {
            result.add(raw.toString().trim().toUpperCase());
        }
        return result;
    }

    private static Set<String> correctAnswersOf(QuizQuestion question) {
        Object correct = question.getCorrectAnswers() == null ? null : question.getCorrectAnswers().get("correct");
        return normalizeAnswer(correct);
    }
}

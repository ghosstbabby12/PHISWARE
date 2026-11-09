package com.phishware.service;

import com.phishware.dto.response.BadgeResponse;
import com.phishware.entity.Badge;
import com.phishware.entity.User;
import com.phishware.entity.UserBadge;
import com.phishware.repository.BadgeRepository;
import com.phishware.repository.UrlAnalysisRepository;
import com.phishware.repository.UserBadgeRepository;
import com.phishware.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Badges (12 seeded) and activity streaks.
 *
 * Auto-awards the badges whose progress is fully tracked server-side
 * (TOTAL_ANALYSES, THREATS_DETECTED, LOGIN_STREAK_DAYS). EDUCATION, QUIZ and
 * SPECIAL badges stay locked until their progress is tracked.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final UrlAnalysisRepository analysisRepository;

    @Transactional(readOnly = true)
    public List<BadgeResponse> listBadges(Long userId) {
        Set<Long> earned = userBadgeRepository.findBadgeIdsByUserId(userId).stream()
            .collect(Collectors.toSet());
        Map<Long, java.time.LocalDateTime> earnedAt = userBadgeRepository.findByUserId(userId)
            .stream()
            .collect(Collectors.toMap(ub -> ub.getBadge().getId(), UserBadge::getEarnedAt, (a, b) -> a));
        return badgeRepository.findAllByOrderByIdAsc().stream()
            .map(b -> BadgeResponse.builder()
                .id(b.getId())
                .name(b.getName())
                .description(b.getDescription())
                .iconUrl(b.getIconUrl())
                .badgeType(b.getBadgeType())
                .conditionValue(b.getConditionValue())
                .pointsReward(b.getPointsReward())
                .earned(earned.contains(b.getId()))
                .earnedAt(earnedAt.get(b.getId()))
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public int currentStreakDays(Long userId) {
        List<LocalDate> days = analysisRepository.findAnalysisDatesDesc(userId);
        if (days.isEmpty()) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        LocalDate cursor = days.get(0);
        if (cursor.isBefore(today.minusDays(1))) {
            return 0;
        }
        int streak = 0;
        LocalDate expected = cursor.equals(today) ? today : today.minusDays(1);
        for (LocalDate day : days) {
            if (day.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (day.isBefore(expected)) {
                break;
            }
        }
        return streak;
    }

    /**
     * Awards badges whose progress is tracked after each analysis.
     * Runs inside the caller's transaction where possible; safe to call twice.
     */
    @Transactional
    public void evaluateAnalysisBadges(User user) {
        int streak = currentStreakDays(user.getId());
        evaluate(user, "TOTAL_ANALYSES", u -> u.getTotalAnalyses());
        evaluate(user, "THREATS_DETECTED", u -> u.getThreatsDetected());
        if (streak > 0) {
            evaluateValue(user, "LOGIN_STREAK_DAYS", streak);
        }
    }

    private void evaluate(User user, String conditionType, ToIntFunction<User> progress) {
        int value = progress.applyAsInt(user);
        for (Badge badge : badgeRepository.findAllByOrderByIdAsc()) {
            if (badge.getConditionType().equals(conditionType) && value >= badge.getConditionValue()) {
                award(user, badge);
            }
        }
    }

    private void evaluateValue(User user, String conditionType, int value) {
        for (Badge badge : badgeRepository.findAllByOrderByIdAsc()) {
            if (badge.getConditionType().equals(conditionType) && value >= badge.getConditionValue()) {
                award(user, badge);
            }
        }
    }

    private void award(User user, Badge badge) {
        if (userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
            return;
        }
        UserBadge grant = new UserBadge();
        grant.setUser(user);
        grant.setBadge(badge);
        userBadgeRepository.save(grant);
        userRepository.findById(user.getId()).ifPresent(fresh -> {
            fresh.setPoints(fresh.getPoints() + badge.getPointsReward());
            userRepository.save(fresh);
        });
        log.info("[GAMIFICATION] Badge '{}' otorgada a {}", badge.getName(), user.getUsername());
    }
}

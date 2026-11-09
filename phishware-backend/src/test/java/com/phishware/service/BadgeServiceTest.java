package com.phishware.service;

import com.phishware.entity.Badge;
import com.phishware.entity.User;
import com.phishware.entity.UserBadge;
import com.phishware.repository.BadgeRepository;
import com.phishware.repository.UrlAnalysisRepository;
import com.phishware.repository.UserBadgeRepository;
import com.phishware.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock private BadgeRepository badgeRepository;
    @Mock private UserBadgeRepository userBadgeRepository;
    @Mock private UserRepository userRepository;
    @Mock private UrlAnalysisRepository analysisRepository;

    @InjectMocks
    private BadgeService badgeService;

    private User user() {
        return User.builder().id(1L).username("u").email("u@x.com")
            .totalAnalyses(10).threatsDetected(1).points(0).level(1).build();
    }

    private Badge badge(Long id, String condition, int value) {
        return Badge.builder().id(id).name("B" + id).badgeType("ANALYSIS")
            .conditionType(condition).conditionValue(value).pointsReward(10).build();
    }

    @Test
    void listBadgesMarksEarnedOnes() {
        Badge b1 = badge(1L, "TOTAL_ANALYSES", 1);
        Badge b2 = badge(1L + 1, "TOTAL_ANALYSES", 50);
        when(badgeRepository.findAllByOrderByIdAsc()).thenReturn(List.of(b1, b2));
        when(userBadgeRepository.findBadgeIdsByUserId(1L)).thenReturn(List.of(1L));
        UserBadge grant = new UserBadge();
        grant.setBadge(b1);
        grant.setEarnedAt(java.time.LocalDateTime.now());
        when(userBadgeRepository.findByUserId(1L)).thenReturn(List.of(grant));

        var list = badgeService.listBadges(1L);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).isEarned()).isTrue();
        assertThat(list.get(1).isEarned()).isFalse();
    }

    @Test
    void analysisBadgesAreAwardedOnce() {
        Badge first = badge(1L, "TOTAL_ANALYSES", 10);
        when(badgeRepository.findAllByOrderByIdAsc()).thenReturn(List.of(first));
        when(analysisRepository.findAnalysisDatesDesc(1L))
            .thenReturn(List.of(LocalDate.now()));
        when(userBadgeRepository.existsByUserIdAndBadgeId(1L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));

        badgeService.evaluateAnalysisBadges(user());

        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void alreadyEarnedBadgesAreNotAwardedAgain() {
        Badge first = badge(1L, "TOTAL_ANALYSES", 10);
        when(badgeRepository.findAllByOrderByIdAsc()).thenReturn(List.of(first));
        when(analysisRepository.findAnalysisDatesDesc(1L)).thenReturn(List.of());
        when(userBadgeRepository.existsByUserIdAndBadgeId(1L, 1L)).thenReturn(true);

        badgeService.evaluateAnalysisBadges(user());

        verify(userBadgeRepository, never()).save(any());
    }

    @Test
    void streakCountsConsecutiveDays() {
        LocalDate today = LocalDate.now();
        when(analysisRepository.findAnalysisDatesDesc(1L))
            .thenReturn(List.of(today, today.minusDays(1), today.minusDays(2)));

        assertThat(badgeService.currentStreakDays(1L)).isEqualTo(3);
    }

    @Test
    void streakBreaksOnMissingDay() {
        LocalDate today = LocalDate.now();
        when(analysisRepository.findAnalysisDatesDesc(1L))
            .thenReturn(List.of(today, today.minusDays(2)));

        assertThat(badgeService.currentStreakDays(1L)).isEqualTo(1);
    }

    @Test
    void emptyHistoryMeansNoStreak() {
        when(analysisRepository.findAnalysisDatesDesc(1L)).thenReturn(List.of());

        assertThat(badgeService.currentStreakDays(1L)).isEqualTo(0);
    }
}

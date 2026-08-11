package com.phishware.service;

import com.phishware.entity.User;
import com.phishware.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private final UserRepository userRepository;

    @Async
    @Transactional
    public void onUrlAnalyzed(User user, boolean threatDetected) {
        int pointsEarned = threatDetected ? 15 : 5;
        int newTotalAnalyses = user.getTotalAnalyses() + 1;
        int newThreatsDetected = user.getThreatsDetected() + (threatDetected ? 1 : 0);
        int newPoints = user.getPoints() + pointsEarned;
        int newLevel = calculateLevel(newPoints);

        user.setTotalAnalyses(newTotalAnalyses);
        user.setThreatsDetected(newThreatsDetected);
        user.setPoints(newPoints);
        user.setLevel(newLevel);
        userRepository.save(user);

        log.debug("Usuario {} ganó {} puntos. Total: {} - Nivel: {}",
            user.getUsername(), pointsEarned, newPoints, newLevel);
    }

    @Async
    @Transactional
    public void onQuizCompleted(User user, int pointsEarned) {
        int newPoints = user.getPoints() + pointsEarned;
        int newLevel = calculateLevel(newPoints);
        user.setPoints(newPoints);
        user.setLevel(newLevel);
        userRepository.save(user);
    }

    // Niveles basados en puntos: 1=0-99, 2=100-249, ..., 10=2000+
    private int calculateLevel(int points) {
        if (points < 100)  return 1;
        if (points < 250)  return 2;
        if (points < 500)  return 3;
        if (points < 800)  return 4;
        if (points < 1200) return 5;
        if (points < 1700) return 6;
        if (points < 2300) return 7;
        if (points < 3000) return 8;
        if (points < 4000) return 9;
        return 10;
    }

    public int getPointsForNextLevel(int currentPoints) {
        int level = calculateLevel(currentPoints);
        return switch (level) {
            case 1 -> 100;
            case 2 -> 250;
            case 3 -> 500;
            case 4 -> 800;
            case 5 -> 1200;
            case 6 -> 1700;
            case 7 -> 2300;
            case 8 -> 3000;
            case 9 -> 4000;
            default -> currentPoints;
        };
    }
}

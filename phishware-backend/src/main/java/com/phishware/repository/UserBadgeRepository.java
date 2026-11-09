package com.phishware.repository;

import com.phishware.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadge.UserBadgeId> {

    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    java.util.List<UserBadge> findByUserId(Long userId);

    @Query("SELECT ub.badge.id FROM UserBadge ub WHERE ub.user.id = :userId")
    List<Long> findBadgeIdsByUserId(Long userId);
}

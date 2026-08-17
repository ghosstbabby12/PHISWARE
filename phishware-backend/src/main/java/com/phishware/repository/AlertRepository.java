package com.phishware.repository;

import com.phishware.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.user.id = :userId")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.id = :alertId AND a.user.id = :userId")
    void markAsRead(@Param("alertId") Long alertId, @Param("userId") Long userId);
}

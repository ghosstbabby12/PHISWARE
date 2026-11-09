package com.phishware.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_badges")
@Getter
@Setter
@NoArgsConstructor
@IdClass(UserBadge.UserBadgeId.class)
public class UserBadge {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @PrePersist
    protected void onCreate() {
        earnedAt = LocalDateTime.now();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBadgeId implements Serializable {
        private Long user;
        private Long badge;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserBadgeId other)) return false;
            return Objects.equals(user, other.user) && Objects.equals(badge, other.badge);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, badge);
        }
    }
}

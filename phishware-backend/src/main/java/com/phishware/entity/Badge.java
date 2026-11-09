package com.phishware.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "badge_type", nullable = false, length = 50)
    private String badgeType;

    @Column(name = "condition_type", nullable = false, length = 80)
    private String conditionType;

    @Column(name = "condition_value", nullable = false)
    private int conditionValue;

    @Column(name = "points_reward", nullable = false)
    private int pointsReward;
}

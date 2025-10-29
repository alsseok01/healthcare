package org.hknu.healthcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Mission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private enums.MissionType type;

    @Column(nullable = false, length = 120)
    private String title;            // 예: "오늘 복약 3회 인증"
    @Column(length = 1000)
    private String description;

    private long rewardPoint;        // 달성 보상
    private boolean active = true;
}

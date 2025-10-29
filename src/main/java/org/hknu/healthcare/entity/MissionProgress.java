package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "user_id,mission_id,status"))
public class MissionProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Mission mission;

    @Enumerated(EnumType.STRING)
    private enums.MissionStatus status = enums.MissionStatus.ASSIGNED;

    private int progress;      // 예: 현재 달성 횟수
    private int goal;          // 예: 목표 횟수
    private LocalDate startDate;
    private LocalDate endDate;
}

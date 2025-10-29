package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "profile_id,startDate"))
public class Prescription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private PersonProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    private Pill pill;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private enums.IntakeUnit unit = enums.IntakeUnit.TAB;

    private Integer dosePerIntake; // 1회 복용 수량
    private Integer timesPerDay;   // 1일 복용 횟수
    private String directions;     // 복용법 메모
}
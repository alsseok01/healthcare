package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "profile_id"))
public class CheckupRecommendation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private PersonProfile profile;

    @Column(nullable = false, length = 80)
    private String checkupType; // 예: "위내시경","혈액검사"

    private LocalDate recommendedDate;
    @Column(length = 500) private String reason;
}
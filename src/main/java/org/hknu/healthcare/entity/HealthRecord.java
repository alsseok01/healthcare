package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "profile_id,recordDate"))
public class HealthRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private PersonProfile profile;

    private LocalDate recordDate;

    @Column(nullable = false, length = 80)
    private String category; // 예: "혈압","혈당","콜레스테롤","건강검진"

    @Column(length = 2000)
    private String valueJson; // 단위/상세수치 JSON 저장
}

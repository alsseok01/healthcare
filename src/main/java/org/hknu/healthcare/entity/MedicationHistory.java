package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "user_id"))
public class MedicationHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private String pillName; // 알약 이름 (단순 문자열 저장)

    private LocalDate startDate;
    private LocalDate endDate; // 복용 기간 (종료일)

    // 알람이 종료되거나 삭제되었을 때 이 테이블로 데이터를 이관합니다.
}
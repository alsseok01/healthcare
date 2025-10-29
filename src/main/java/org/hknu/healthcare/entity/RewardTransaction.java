package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "account_id,createdAt"))
public class RewardTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private RewardAccount account;

    @Enumerated(EnumType.STRING)
    private enums.RewardTxnType type;

    private long amount;                // + 적립 / - 사용
    @Column(length = 200) private String reason; // 예: "건강검진 예약 완료"
}

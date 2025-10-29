package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class RewardAccount extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private long balance = 0L; // 현재 포인트
}

package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "user_id,createdAt"))
public class RewardOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private RewardItem item;

    private int quantity;
    private long totalPrice; // 스냅샷
    private String shippingInfo; // 쿠폰/코드인 경우 null
}

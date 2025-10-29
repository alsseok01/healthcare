package org.hknu.healthcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RewardItem extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;
    private String imageUrl;
    private long price;     // 포인트 가격
    private boolean active = true;
    private int stock = 0;  // 필요 시
}

package org.hknu.healthcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Household extends BaseEntity {

    @Column(nullable = false, length = 60)
    private String name; // 예: "우리집"

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner; // 그룹 생성자

    // 구성원 수, 초대 코드 등 부가정보
    private String inviteCode;
}
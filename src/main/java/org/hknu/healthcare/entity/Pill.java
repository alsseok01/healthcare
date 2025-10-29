package org.hknu.healthcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(indexes = {@Index(columnList="name"), @Index(columnList="code")})
public class Pill extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;          // 품목명
    @Column(length = 60)
    private String code;          // 식약처 품목/식별 코드
    @Column(length = 1000)
    private String description;   // 효능/주의 등 요약
    private String imageUrl;      // 알약 이미지
}

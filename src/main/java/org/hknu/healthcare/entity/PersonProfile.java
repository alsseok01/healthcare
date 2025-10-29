package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "user_id"))
public class PersonProfile extends BaseEntity {

    // 실제 주인(회원 본인 프로필 1:1 권장)
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    // 가족 안에서 관리하는 대상(예: 부모님 계정 없이도 관리)
    @ManyToOne(fetch = FetchType.LAZY)
    private Household household;

    @Column(nullable = false, length = 40)
    private String displayName;   // 화면에 표시할 이름

    private LocalDate birthDate;
    @Enumerated(EnumType.STRING)
    private enums.Gender gender;

    // 기저질환/알레르기/의료 메모
    @Column(length = 2000)
    private String medicalNote;
}

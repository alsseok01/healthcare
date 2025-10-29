package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 60)
    private String passwordHash;

    @Column(nullable = false, length = 40)
    private String name;

    private LocalDate birthDate;
    @Enumerated(EnumType.STRING)
    private enums.Gender gender;

    // 내 건강 설정/선호 등
    private String phone;
    private String profileImageUrl;
}

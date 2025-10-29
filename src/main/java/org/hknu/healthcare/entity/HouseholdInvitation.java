package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class HouseholdInvitation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) private Household household;

    @Column(nullable = false) private String email; // 초대 대상
    @Enumerated(EnumType.STRING) private enums.InvitationStatus status = enums.InvitationStatus.PENDING;

    @Column(length = 200) private String message;
    private LocalDateTime expiresAt;
}

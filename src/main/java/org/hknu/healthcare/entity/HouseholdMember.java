package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"household_id","user_id"}))
public class HouseholdMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) private Household household;
    @ManyToOne(fetch = FetchType.LAZY) private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private enums.RelationRole role = enums.RelationRole.OTHER;

    private boolean canViewHealth = true;   // 건강정보 열람 권한
    private boolean canEditHealth = false;  // 수정 권한
}
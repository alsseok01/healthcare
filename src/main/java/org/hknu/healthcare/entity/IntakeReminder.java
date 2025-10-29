package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "prescription_id,status"))
public class IntakeReminder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Prescription prescription;

    @Enumerated(EnumType.STRING)
    private enums.ReminderStatus status = enums.ReminderStatus.ACTIVE;

    // 매일 08:00/13:00/20:00 형태를 간단히 저장(복수 알림은 여러 행)
    private LocalTime remindAt;

    private Boolean mon=true, tue=true, wed=true, thu=true, fri=true, sat=true, sun=true;

    private LocalDate validFrom;
    private LocalDate validUntil;
}

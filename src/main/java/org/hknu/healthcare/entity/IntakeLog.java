package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "reminder_id,takenAt"))
public class IntakeLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private IntakeReminder reminder;

    private LocalDateTime takenAt;   // 실제 복용 시각
    private boolean taken;           // 복용여부
    private boolean snoozed;         // 미룸
    @Column(length = 300)
    private String note;
}

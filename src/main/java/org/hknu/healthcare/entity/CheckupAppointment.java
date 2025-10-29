package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "profile_id,status,appointmentDateTime"))
public class CheckupAppointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private PersonProfile profile;

    @Column(nullable = false, length = 80)
    private String checkupType;

    private LocalDateTime appointmentDateTime;
    private String hospitalName;
    private String department;
    private String reservationNumber;

    @Enumerated(EnumType.STRING)
    private enums.AppointmentStatus status = enums.AppointmentStatus.RESERVED; // 예약→확정→완료

    @Column(length = 1000)
    private String note;
}

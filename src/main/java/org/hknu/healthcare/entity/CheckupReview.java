package org.hknu.healthcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(indexes = @Index(columnList = "appointment_id"))
public class CheckupReview extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    private CheckupAppointment appointment;

    @Enumerated(EnumType.STRING)
    private enums.ReviewRating rating;

    @Column(length = 1000)
    private String comment;
}

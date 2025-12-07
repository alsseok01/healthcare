package org.hknu.healthcare.Repo;

import org.hknu.healthcare.entity.IntakeReminder;
import org.hknu.healthcare.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntakeReminderRepository extends JpaRepository<IntakeReminder, Long> {
    List<IntakeReminder> findByPrescription(Prescription prescription);
}

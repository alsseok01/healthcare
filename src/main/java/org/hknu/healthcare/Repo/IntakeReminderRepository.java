package org.hknu.healthcare.Repo;

import org.hknu.healthcare.entity.IntakeReminder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntakeReminderRepository extends JpaRepository<IntakeReminder, Long> {
}

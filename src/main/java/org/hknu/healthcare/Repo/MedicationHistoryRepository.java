package org.hknu.healthcare.Repo;

import org.hknu.healthcare.entity.MedicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationHistoryRepository extends JpaRepository<MedicationHistory, Long> {
    List<MedicationHistory> findByUserId(Long userId);
}

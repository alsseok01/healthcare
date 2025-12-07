package org.hknu.healthcare.Repo;

import org.hknu.healthcare.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByEndDateBeforeAndDeletedFalse(LocalDate date);
    List<Prescription> findByProfile_User_IdAndDeletedFalseOrderByIdAsc(Long userId);
}

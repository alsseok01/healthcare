package org.hknu.healthcare.Repo;

import org.hknu.healthcare.entity.PersonProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonProfileRepository extends JpaRepository<PersonProfile, Long> {
    Optional<PersonProfile> findByUserId(Long userId);
}

package org.hknu.healthcare.Repo;

import org.hknu.healthcare.entity.Pill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PillRepository extends JpaRepository<Pill, Long> {
    // 약 이름으로 약 정보 찾기 (중복 방지 및 검색용)
    Optional<Pill> findByName(String name);
}

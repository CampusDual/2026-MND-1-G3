package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.FarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface FarmRepository extends JpaRepository<FarmEntity, Long> {
    boolean existsByRegaCode(String regaCode);
    Optional<FarmEntity> findByRegaCode(String regaCode);
    List<FarmEntity> findByClinicaId(Long clinicaId);
}

package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.GanaderiaOperacionCampoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GanaderiaOperacionCampoRepository
        extends JpaRepository<GanaderiaOperacionCampoEntity, Long> {
    Optional<GanaderiaOperacionCampoEntity> findByClinicaIdAndOperationId(
            Long clinicaId, String operationId);
}

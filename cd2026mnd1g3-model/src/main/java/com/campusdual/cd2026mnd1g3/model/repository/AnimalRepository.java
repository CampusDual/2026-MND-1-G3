package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Long> {
    boolean existsByEarTag(String earTag);
    List<AnimalEntity> findByFarmId(Long farmId);
    List<AnimalEntity> findByFechaFinRetiradaLessThanEqualAndEstadoSanitario(
            java.time.LocalDate date, EstadoSanitario estadoSanitario);
}

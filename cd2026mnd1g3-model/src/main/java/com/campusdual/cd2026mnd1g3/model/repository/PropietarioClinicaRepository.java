package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.PropietarioClinicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropietarioClinicaRepository extends JpaRepository<PropietarioClinicaEntity, Long> {
    Optional<PropietarioClinicaEntity> findByIdAndClinicaIdAndActivoTrue(Long id, Long clinicaId);
    Optional<PropietarioClinicaEntity> findByClinicaIdAndTelefonoAndActivoTrue(Long clinicaId, String telefono);
    Optional<PropietarioClinicaEntity> findByClinicaIdAndDniNifAndActivoTrue(Long clinicaId, String dniNif);
    @Query("""
            SELECT owner FROM PropietarioClinicaEntity owner
            WHERE owner.clinicaId = :clinicaId
              AND owner.activo = true
              AND (
                   LOWER(owner.nombreNormalizado) LIKE LOWER(CONCAT('%', :termino, '%'))
                   OR owner.telefono LIKE CONCAT('%', :termino, '%')
                   OR LOWER(COALESCE(owner.dniNif, '')) LIKE LOWER(CONCAT('%', :termino, '%'))
              )
            """)
    List<PropietarioClinicaEntity> searchByTerm(
            @Param("clinicaId") Long clinicaId,
            @Param("termino") String termino);
}

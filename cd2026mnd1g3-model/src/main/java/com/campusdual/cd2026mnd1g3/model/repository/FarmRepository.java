package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.FarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FarmRepository extends JpaRepository<FarmEntity, Long> {
    boolean existsByRegaCode(String regaCode);
    Optional<FarmEntity> findByRegaCode(String regaCode);
    List<FarmEntity> findByClinicaId(Long clinicaId);

    @Query("""
            select f
            from FarmEntity f
            where f.clinicaId = :clinicaId
              and (:search is null
                   or lower(coalesce(f.name, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(f.regaCode, '')) like lower(concat('%', :search, '%')))
            """)
    Page<FarmEntity> findHierarchyPage(
            @Param("clinicaId") Long clinicaId,
            @Param("search") String search,
            Pageable pageable);
}

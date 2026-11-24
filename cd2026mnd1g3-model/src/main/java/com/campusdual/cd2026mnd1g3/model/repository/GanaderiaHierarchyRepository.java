package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GanaderiaHierarchyRepository extends JpaRepository<AnimalEntity, Long> {

    @Query("""
            select a.farm.id, coalesce(sum(a.cantidadCabezas), 0)
            from AnimalEntity a
            where a.clinicaId = :clinicaId
            group by a.farm.id
            """)
    List<Object[]> headsByFarm(@Param("clinicaId") Long clinicaId);

    @Query("""
            select coalesce(a.subtipo, 'Sin clasificar'),
                   coalesce(sum(a.cantidadCabezas), 0)
            from AnimalEntity a
            where a.clinicaId = :clinicaId
              and a.farm.id = :farmId
            group by coalesce(a.subtipo, 'Sin clasificar')
            order by coalesce(a.subtipo, 'Sin clasificar') asc
            """)
    List<Object[]> headsBySubtype(@Param("clinicaId") Long clinicaId,
                                  @Param("farmId") Long farmId);

    @Query("""
            select coalesce(a.ubicacion, 'Sin ubicacion'),
                   coalesce(sum(a.cantidadCabezas), 0)
            from AnimalEntity a
            where a.clinicaId = :clinicaId
              and a.farm.id = :farmId
            group by coalesce(a.ubicacion, 'Sin ubicacion')
            order by coalesce(a.ubicacion, 'Sin ubicacion') asc
            """)
    List<Object[]> headsByLocation(@Param("clinicaId") Long clinicaId,
                                   @Param("farmId") Long farmId);

    @Query("""
            select a.estadoSanitario,
                   coalesce(sum(a.cantidadCabezas), 0)
            from AnimalEntity a
            where a.clinicaId = :clinicaId
              and a.farm.id = :farmId
            group by a.estadoSanitario
            """)
    List<Object[]> headsByState(@Param("clinicaId") Long clinicaId,
                                @Param("farmId") Long farmId);

    @Query("""
            select a
            from AnimalEntity a
            where a.clinicaId = :clinicaId
              and a.farm.id = :farmId
              and (:type <> 'SUBTIPO'
                   or coalesce(a.subtipo, 'Sin clasificar') = :groupId)
              and (:type <> 'UBICACION'
                   or coalesce(a.ubicacion, 'Sin ubicacion') = :groupId)
              and (:search is null
                   or lower(coalesce(a.earTag, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(a.name, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(a.notas, '')) like lower(concat('%', :search, '%')))
            """)
    Page<AnimalEntity> findGroupPage(
            @Param("clinicaId") Long clinicaId,
            @Param("farmId") Long farmId,
            @Param("type") String type,
            @Param("groupId") String groupId,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
            select a
            from AnimalEntity a
            where a.clinicaId = :clinicaId
              and a.farm.id = :farmId
              and a.estadoSanitario = :state
              and (:search is null
                   or lower(coalesce(a.earTag, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(a.name, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(a.notas, '')) like lower(concat('%', :search, '%')))
            """)
    Page<AnimalEntity> findStatePage(
            @Param("clinicaId") Long clinicaId,
            @Param("farmId") Long farmId,
            @Param("state") EstadoSanitario state,
            @Param("search") String search,
            Pageable pageable);
}

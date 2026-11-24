package com.campusdual.cd2026mnd1g3.api.service;

import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaAnimalPageDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaFarmPageDto;
import com.campusdual.cd2026mnd1g3.api.dto.AltaGanaderiaAnimalRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.AnimalDto;
import com.campusdual.cd2026mnd1g3.api.dto.ClasificacionGanaderiaRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.OpcionesClasificacionDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaFarmSummaryDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaGroupDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaEstadoMasivoRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaProtocoloMasivoRequestDto;
import com.campusdual.cd2026mnd1g3.api.enums.GanaderiaGroupType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GanaderiaHierarchyService {
    GanaderiaFarmPageDto findFarms(Long clinicaId, String search, Pageable pageable);
    List<GanaderiaGroupDto> findGroups(Long clinicaId, Long farmId, GanaderiaGroupType type);
    GanaderiaAnimalPageDto findAnimals(Long clinicaId, Long farmId,
                                       GanaderiaGroupType type, String groupId,
                                       String search, Pageable pageable);
    AnimalDto altaAnimal(Long clinicaId, Long farmId,
                         AltaGanaderiaAnimalRequestDto request);
    void clasificarAnimales(Long clinicaId, Long farmId,
                            ClasificacionGanaderiaRequestDto request);
    OpcionesClasificacionDto findOpcionesClasificacion(Long clinicaId, Long farmId);
    int aplicarEstadoMasivo(Long clinicaId, Long farmId,
                            GanaderiaEstadoMasivoRequestDto request);
    int aplicarProtocoloMasivo(Long clinicaId, Long farmId,
                               GanaderiaProtocoloMasivoRequestDto request);
    AnimalDto findByCrotal(Long clinicaId, Long farmId, String crotal);
}

package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.Species;

import java.util.List;

public record GanaderiaFarmSummaryDto(
        Long ownerId,
        Long farmId,
        Long clinicaId,
        String name,
        String regaCode,
        Species mainSpecies,
        long totalHeads,
        List<GanaderiaSubtypeDto> subtypes,
        long activeAlerts,
        long pendingActions
) {}

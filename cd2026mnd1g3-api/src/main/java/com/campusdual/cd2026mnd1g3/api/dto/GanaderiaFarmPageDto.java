package com.campusdual.cd2026mnd1g3.api.dto;

import java.util.List;

public record GanaderiaFarmPageDto(
        List<GanaderiaFarmSummaryDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}

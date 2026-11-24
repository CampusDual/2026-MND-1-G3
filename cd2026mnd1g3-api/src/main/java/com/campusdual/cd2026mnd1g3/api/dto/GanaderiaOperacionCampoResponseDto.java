package com.campusdual.cd2026mnd1g3.api.dto;

import java.time.LocalDateTime;

public record GanaderiaOperacionCampoResponseDto(
        Long id,
        String operationId,
        Long farmId,
        Long animalId,
        String tipoOperacion,
        String detalle,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaSincronizacion
) {}

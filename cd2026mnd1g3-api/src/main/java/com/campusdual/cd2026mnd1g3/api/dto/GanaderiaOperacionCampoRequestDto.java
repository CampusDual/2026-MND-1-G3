package com.campusdual.cd2026mnd1g3.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GanaderiaOperacionCampoRequestDto(
        @NotBlank @Size(max = 64) String operationId,
        @NotNull Long farmId,
        Long animalId,
        @NotBlank @Size(max = 40) String tipoOperacion,
        @Size(max = 2000) String detalle
) {}

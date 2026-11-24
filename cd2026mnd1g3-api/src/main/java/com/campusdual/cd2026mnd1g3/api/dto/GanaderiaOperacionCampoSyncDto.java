package com.campusdual.cd2026mnd1g3.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record GanaderiaOperacionCampoSyncDto(
        @NotEmpty List<@Valid GanaderiaOperacionCampoRequestDto> operaciones
) {}

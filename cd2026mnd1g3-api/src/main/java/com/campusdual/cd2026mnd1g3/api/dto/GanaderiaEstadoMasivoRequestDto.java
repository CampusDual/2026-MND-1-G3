package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GanaderiaEstadoMasivoRequestDto(
        @NotEmpty List<Long> animalIds,
        @NotNull EstadoSanitario estadoSanitario
) {}

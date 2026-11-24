package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ClasificacionGanaderiaRequestDto(
        @NotEmpty List<Long> animalIds,
        @Size(max = 40) String subtipo,
        @Size(max = 80) String ubicacion,
        EstadoSanitario estadoSanitario
) {}

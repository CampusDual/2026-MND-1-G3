package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.Species;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AltaGanaderiaAnimalRequestDto(
        @NotBlank @Size(max = 40) String crotal,
        @Size(max = 100) String nombre,
        Species especie,
        @Size(max = 40) String subtipo,
        @Size(max = 80) String ubicacion,
        EstadoSanitario estadoSanitario,
        @Min(1) Integer cabezas
) {}

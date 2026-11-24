package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.EstadoAvisoClinico;
import com.campusdual.cd2026mnd1g3.api.enums.MotivoDescarteAviso;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CambioEstadoAvisoDto(
        @NotNull EstadoAvisoClinico estado,
        LocalDate nuevaFecha,
        MotivoDescarteAviso motivoDescarte,
        @jakarta.validation.constraints.Size(max = 500) String motivoTexto,
        Boolean confirmarFallecimiento
) {}

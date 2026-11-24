package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.CategoriaAvisoClinico;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AvisoClinicoRequestDto(
        @NotNull Long pacienteId,
        @NotNull CategoriaAvisoClinico categoria,
        @NotBlank @Size(max = 150) String titulo,
        @Size(max = 2000) String detalleClinico,
        @NotNull LocalDate fechaObjetivo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        @Size(max = 30) String progreso
) {}

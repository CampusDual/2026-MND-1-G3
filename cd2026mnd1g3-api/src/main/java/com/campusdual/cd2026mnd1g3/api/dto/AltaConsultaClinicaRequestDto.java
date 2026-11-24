package com.campusdual.cd2026mnd1g3.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AltaConsultaClinicaRequestDto(
        @NotNull Long pacienteId,
        @NotNull LocalDateTime fechaConsulta,
        @Size(max = 500) String motivo,
        @Size(max = 2000) String notas,
        LocalDate proximaVacuna
) {}

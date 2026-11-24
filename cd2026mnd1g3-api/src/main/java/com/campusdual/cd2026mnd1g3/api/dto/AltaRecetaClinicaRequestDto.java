package com.campusdual.cd2026mnd1g3.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AltaRecetaClinicaRequestDto(
        @NotNull Long consultaId,
        @NotNull Long pacienteId,
        Long medicamentoId,
        Long loteStockId,
        @Size(max = 1000) String indicaciones,
        @NotNull LocalDate fechaEmision
) {}

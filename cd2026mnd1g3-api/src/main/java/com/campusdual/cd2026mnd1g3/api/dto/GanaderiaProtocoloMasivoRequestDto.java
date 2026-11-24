package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.EventType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record GanaderiaProtocoloMasivoRequestDto(
        @NotEmpty List<Long> animalIds,
        @NotNull EventType eventType,
        @NotBlank @Size(max = 150) String title,
        @NotNull LocalDate applicationDate,
        @Min(0) int withdrawalDays,
        LocalDate withdrawalEndDate,
        @Size(max = 120) String medication,
        Integer nextCycleDays
) {}

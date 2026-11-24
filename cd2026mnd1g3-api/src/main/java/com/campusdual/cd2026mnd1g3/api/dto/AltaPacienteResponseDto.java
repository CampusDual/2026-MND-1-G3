package com.campusdual.cd2026mnd1g3.api.dto;

public record AltaPacienteResponseDto(
        PacienteClinicaDto paciente,
        PropietarioClinicaDto propietario
) {}

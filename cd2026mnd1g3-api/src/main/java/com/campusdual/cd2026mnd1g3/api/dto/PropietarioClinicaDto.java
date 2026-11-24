package com.campusdual.cd2026mnd1g3.api.dto;

public record PropietarioClinicaDto(
        Long id,
        String nombre,
        String apellidos,
        String dniNif,
        String telefono,
        String email,
        boolean notificacionesInternas,
        long mascotasTotales
) {}

package com.campusdual.cd2026mnd1g3.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConsultaClinicaDto(
        Long id,
        Long clinicaId,
        Long pacienteId,
        String nombrePaciente,
        String especie,
        String raza,
        String microchip,
        Long propietarioId,
        String nombrePropietario,
        String apellidosPropietario,
        LocalDateTime fechaConsulta,
        String motivo,
        String notas,
        LocalDate proximaVacuna
) {}

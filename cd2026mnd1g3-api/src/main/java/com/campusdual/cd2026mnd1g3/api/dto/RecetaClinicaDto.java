package com.campusdual.cd2026mnd1g3.api.dto;

import java.time.LocalDate;

public record RecetaClinicaDto(
        Long id,
        Long clinicaId,
        Long consultaId,
        Long pacienteId,
        String nombrePaciente,
        Long medicamentoId,
        Long loteStockId,
        String indicaciones,
        LocalDate fechaEmision
) {}

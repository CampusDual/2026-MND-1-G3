package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoInventarioDto(
        Long id,
        Long clinicaId,
        Long loteStockId,
        TipoMovimiento tipoMovimiento,
        double cantidad,
        LocalDateTime fechaHora,
        Long veterinarioId,
        Long animalId,
        Long explotacionId,
        Long protocoloEventoId
) {}

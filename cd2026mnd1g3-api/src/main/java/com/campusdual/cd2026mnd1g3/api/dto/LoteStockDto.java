package com.campusdual.cd2026mnd1g3.api.dto;

import java.time.LocalDate;

public record LoteStockDto(
        Long id,
        Long medicamentoId,
        String numeroLote,
        LocalDate fechaCaducidad,
        String proveedor
) {}

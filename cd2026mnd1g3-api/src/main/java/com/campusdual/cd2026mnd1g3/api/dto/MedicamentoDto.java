package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.CategoriaPran;

public record MedicamentoDto(
        Long id,
        Long clinicaId,
        String nombreComercial,
        String principioActivo,
        String especieDestino,
        CategoriaPran categoriaPran,
        int diasRetiradaDefecto,
        double stockMinimoAlerta
) {}

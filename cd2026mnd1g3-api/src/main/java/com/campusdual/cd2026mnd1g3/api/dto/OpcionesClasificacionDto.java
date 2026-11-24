package com.campusdual.cd2026mnd1g3.api.dto;

import java.util.List;

public record OpcionesClasificacionDto(
        List<String> subtipos,
        List<String> ubicaciones,
        List<String> subtiposRecomendados,
        List<String> ubicacionesRecomendadas
) {}

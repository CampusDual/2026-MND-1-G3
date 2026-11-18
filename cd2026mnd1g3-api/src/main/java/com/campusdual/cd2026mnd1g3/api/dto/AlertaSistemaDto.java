package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.DestinatarioRol;
import com.campusdual.cd2026mnd1g3.api.enums.TipoAlerta;

import java.time.LocalDateTime;

public record AlertaSistemaDto(
        Long id,
        Long clinicaId,
        DestinatarioRol destinatarioRol,
        TipoAlerta tipoAlerta,
        String titulo,
        String mensaje,
        LocalDateTime fechaProgramada,
        boolean leida,
        boolean confirmadaPorGranja,
        Long explotacionId,
        Long animalId
) {}

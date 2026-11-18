package com.campusdual.cd2026mnd1g3.model.event;

import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;

public record EstadoSanitarioCambiadoEvent(
        Long clinicaId,
        Long explotacionId,
        Long animalId,
        EstadoSanitario nuevoEstado,
        String notas
) {}

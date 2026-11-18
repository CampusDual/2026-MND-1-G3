package com.campusdual.cd2026mnd1g3.model.event;

public record ProtocoloRegistradoEvent(
        Long protocoloId,
        Long clinicaId,
        Long explotacionId,
        Long animalId,
        String titulo,
        boolean conRetirada,
        java.time.LocalDateTime fechaProgramada
) {}

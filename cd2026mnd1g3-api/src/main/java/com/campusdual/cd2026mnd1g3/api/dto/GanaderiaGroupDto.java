package com.campusdual.cd2026mnd1g3.api.dto;

public record GanaderiaGroupDto(
        String groupId,
        String label,
        long heads,
        double percentage,
        long activeAlerts,
        long pendingActions
) {}

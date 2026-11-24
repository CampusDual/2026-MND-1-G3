package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaOperacionCampoRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaOperacionCampoResponseDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaOperacionCampoSyncDto;
import com.campusdual.cd2026mnd1g3.api.service.GanaderiaFieldOperationService;
import com.campusdual.cd2026mnd1g3.ws.service.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ganaderia/operaciones-campo")
public class GanaderiaFieldOperationController {
    private final GanaderiaFieldOperationService service;
    private final TenantContext tenantContext;

    public GanaderiaFieldOperationController(
            GanaderiaFieldOperationService service,
            TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/sync")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN','ROLE_FARM_MANAGER')")
    public ResponseEntity<List<GanaderiaOperacionCampoResponseDto>> sync(
            @Valid @RequestBody GanaderiaOperacionCampoSyncDto request) {
        return ResponseEntity.ok(service.sync(
                tenantContext.getClinicaId(), request.operaciones()));
    }
}

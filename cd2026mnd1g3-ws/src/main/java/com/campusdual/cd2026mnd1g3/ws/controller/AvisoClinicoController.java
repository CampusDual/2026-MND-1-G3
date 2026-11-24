package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.AvisoClinicoDto;
import com.campusdual.cd2026mnd1g3.api.dto.AvisoClinicoRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.CambioEstadoAvisoDto;
import com.campusdual.cd2026mnd1g3.api.dto.PageResponseDto;
import com.campusdual.cd2026mnd1g3.api.service.AvisoClinicoService;
import com.campusdual.cd2026mnd1g3.ws.service.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/clinica/avisos")
public class AvisoClinicoController {
    private final AvisoClinicoService service;
    private final TenantContext tenantContext;

    public AvisoClinicoController(AvisoClinicoService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<PageResponseDto<AvisoClinicoDto>> findAvisos(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.findAvisos(
                tenantContext.getClinicaId(), pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<AvisoClinicoDto> crear(
            @Valid @RequestBody AvisoClinicoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.crear(tenantContext.getClinicaId(), request));
    }

    @PostMapping("/{avisoId}/estado")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<AvisoClinicoDto> cambiarEstado(
            @PathVariable Long avisoId,
            @Valid @RequestBody CambioEstadoAvisoDto request,
            Authentication authentication) {
        return ResponseEntity.ok(service.cambiarEstado(
                tenantContext.getClinicaId(), avisoId, request,
                authentication.getName()));
    }

    @PostMapping("/{avisoId}/intentado")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<AvisoClinicoDto> marcarIntentado(
            @PathVariable Long avisoId,
            Authentication authentication) {
        return ResponseEntity.ok(service.marcarIntentado(
                tenantContext.getClinicaId(), avisoId, authentication.getName()));
    }

    @PostMapping("/{avisoId}/posponer")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<AvisoClinicoDto> posponer(
            @PathVariable Long avisoId,
            @RequestParam LocalDate nuevaFecha,
            Authentication authentication) {
        return ResponseEntity.ok(service.posponer(
                tenantContext.getClinicaId(), avisoId, nuevaFecha,
                authentication.getName()));
    }
}

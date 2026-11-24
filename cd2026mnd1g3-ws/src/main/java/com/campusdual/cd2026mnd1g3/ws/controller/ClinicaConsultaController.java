package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.*;
import com.campusdual.cd2026mnd1g3.api.service.ClinicaConsultaService;
import com.campusdual.cd2026mnd1g3.ws.service.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinica")
public class ClinicaConsultaController {
    private final ClinicaConsultaService service;
    private final TenantContext tenantContext;

    public ClinicaConsultaController(ClinicaConsultaService service,
                                     TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/consultas")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<List<ConsultaClinicaDto>> findConsultas() {
        return ResponseEntity.ok(service.findConsultas(tenantContext.getClinicaId()));
    }

    @PostMapping("/consultas")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<ConsultaClinicaDto> crearConsulta(
            @Valid @RequestBody AltaConsultaClinicaRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.crearConsulta(tenantContext.getClinicaId(), request));
    }

    @GetMapping("/recetas")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<List<RecetaClinicaDto>> findRecetas() {
        return ResponseEntity.ok(service.findRecetas(tenantContext.getClinicaId()));
    }

    @PostMapping("/recetas")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<RecetaClinicaDto> crearReceta(
            @Valid @RequestBody AltaRecetaClinicaRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.crearReceta(tenantContext.getClinicaId(), request));
    }
}

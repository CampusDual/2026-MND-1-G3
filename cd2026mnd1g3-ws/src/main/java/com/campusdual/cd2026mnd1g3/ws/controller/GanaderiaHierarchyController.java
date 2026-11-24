package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaAnimalPageDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaFarmSummaryDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaGroupDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaFarmPageDto;
import com.campusdual.cd2026mnd1g3.api.dto.AltaGanaderiaAnimalRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.AnimalDto;
import com.campusdual.cd2026mnd1g3.api.dto.ClasificacionGanaderiaRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.OpcionesClasificacionDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaEstadoMasivoRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaProtocoloMasivoRequestDto;
import com.campusdual.cd2026mnd1g3.api.enums.GanaderiaGroupType;
import com.campusdual.cd2026mnd1g3.api.service.GanaderiaHierarchyService;
import com.campusdual.cd2026mnd1g3.ws.service.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/ganaderia")
public class GanaderiaHierarchyController {
    private final GanaderiaHierarchyService service;
    private final TenantContext tenantContext;

    public GanaderiaHierarchyController(GanaderiaHierarchyService service,
                                        TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/explotaciones/resumen")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN','ROLE_FARM_MANAGER')")
    public ResponseEntity<GanaderiaFarmPageDto> findFarms(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.findFarms(
                tenantContext.getClinicaId(), search, pageable));
    }

    @GetMapping("/explotaciones/{farmId}/grupos")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN','ROLE_FARM_MANAGER')")
    public ResponseEntity<List<GanaderiaGroupDto>> findGroups(
            @PathVariable Long farmId,
            @RequestParam GanaderiaGroupType type) {
        return ResponseEntity.ok(service.findGroups(
                tenantContext.getClinicaId(), farmId, type));
    }

    @GetMapping("/explotaciones/{farmId}/grupos/{groupId}/animales")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN','ROLE_FARM_MANAGER')")
    public ResponseEntity<GanaderiaAnimalPageDto> findAnimals(
            @PathVariable Long farmId,
            @PathVariable String groupId,
            @RequestParam GanaderiaGroupType type,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.findAnimals(
                tenantContext.getClinicaId(), farmId, type,
                groupId, search, pageable));
    }

    @PostMapping("/explotaciones/{farmId}/animales")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN','ROLE_FARM_MANAGER')")
    public ResponseEntity<AnimalDto> altaAnimal(
            @PathVariable Long farmId,
            @Valid @RequestBody AltaGanaderiaAnimalRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.altaAnimal(tenantContext.getClinicaId(), farmId, request));
    }

    @PatchMapping("/explotaciones/{farmId}/animales/clasificar")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN','ROLE_FARM_MANAGER')")
    public ResponseEntity<Void> clasificarAnimales(
            @PathVariable Long farmId,
            @Valid @RequestBody ClasificacionGanaderiaRequestDto request) {
        service.clasificarAnimales(
                tenantContext.getClinicaId(), farmId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/explotaciones/{farmId}/opciones-clasificacion")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN','ROLE_FARM_MANAGER')")
    public ResponseEntity<OpcionesClasificacionDto> findOpcionesClasificacion(
            @PathVariable Long farmId) {
        return ResponseEntity.ok(service.findOpcionesClasificacion(
                tenantContext.getClinicaId(), farmId));
    }

    @PatchMapping("/explotaciones/{farmId}/animales/estado")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN','ROLE_FARM_MANAGER')")
    public ResponseEntity<Integer> aplicarEstadoMasivo(
            @PathVariable Long farmId,
            @Valid @RequestBody GanaderiaEstadoMasivoRequestDto request) {
        return ResponseEntity.ok(service.aplicarEstadoMasivo(
                tenantContext.getClinicaId(), farmId, request));
    }

    @PostMapping("/explotaciones/{farmId}/protocolos/bloque")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN','ROLE_FARM_MANAGER')")
    public ResponseEntity<Integer> aplicarProtocoloMasivo(
            @PathVariable Long farmId,
            @Valid @RequestBody GanaderiaProtocoloMasivoRequestDto request) {
        return ResponseEntity.ok(service.aplicarProtocoloMasivo(
                tenantContext.getClinicaId(), farmId, request));
    }

    @GetMapping("/explotaciones/{farmId}/animales/buscar")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN','ROLE_FARM_MANAGER')")
    public ResponseEntity<AnimalDto> findByCrotal(
            @PathVariable Long farmId,
            @RequestParam String crotal) {
        return ResponseEntity.ok(service.findByCrotal(
                tenantContext.getClinicaId(), farmId, crotal));
    }
}

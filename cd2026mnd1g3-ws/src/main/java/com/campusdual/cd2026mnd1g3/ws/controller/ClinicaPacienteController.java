package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.*;
import com.campusdual.cd2026mnd1g3.api.enums.EspecieMascota;
import com.campusdual.cd2026mnd1g3.api.service.ClinicaPacienteService;
import com.campusdual.cd2026mnd1g3.ws.service.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinica/pacientes")
public class ClinicaPacienteController {
    private final ClinicaPacienteService service;
    private final TenantContext tenantContext;

    public ClinicaPacienteController(ClinicaPacienteService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<PageResponseDto<PacienteClinicaDto>> findPacientes(
            @RequestParam(required = false) String propietario,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String microchip,
            @RequestParam(required = false) Boolean sinMicrochip,
            @RequestParam(required = false) EspecieMascota especie,
            @RequestParam(required = false) String raza,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.findPacientes(
                tenantContext.getClinicaId(), propietario, nombre, microchip,
                sinMicrochip, especie, raza, pageable));
    }

    @GetMapping("/{pacienteId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<PacienteClinicaDto> findPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(service.findPaciente(
                tenantContext.getClinicaId(), pacienteId));
    }

    @GetMapping("/propietarios/buscar")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<List<PropietarioClinicaDto>> buscarPropietarios(
            @RequestParam String termino) {
        return ResponseEntity.ok(service.buscarPropietarios(
                tenantContext.getClinicaId(), termino));
    }

    @GetMapping("/propietarios/duplicado")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<PropietarioClinicaDto> buscarDuplicado(
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String dniNif) {
        return ResponseEntity.ok(service.buscarDuplicadoPropietario(
                tenantContext.getClinicaId(), telefono, dniNif));
    }

    @GetMapping("/propietarios/{propietarioId}/mascotas")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<List<PacienteClinicaDto>> findMascotasPropietario(
            @PathVariable Long propietarioId) {
        return ResponseEntity.ok(service.findMascotasPropietario(
                tenantContext.getClinicaId(), propietarioId));
    }

    @GetMapping("/razas")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<List<String>> findRazas(
            @RequestParam(required = false) EspecieMascota especie) {
        return ResponseEntity.ok(service.findRazas(
                tenantContext.getClinicaId(), especie));
    }

    @PostMapping("/alta")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_VETERINARIAN')")
    public ResponseEntity<AltaPacienteResponseDto> alta(
            @Valid @RequestBody AltaPacienteRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.altaPaciente(tenantContext.getClinicaId(), request));
    }
}

package com.campusdual.cd2026mnd1g3.ws.controller;

import com.campusdual.cd2026mnd1g3.api.dto.PatientDetailDto;
import com.campusdual.cd2026mnd1g3.api.dto.PatientPageDto;
import com.campusdual.cd2026mnd1g3.api.enums.Species;
import com.campusdual.cd2026mnd1g3.model.service.PatientDirectoryService;
import com.campusdual.cd2026mnd1g3.ws.service.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinica/pacientes")
public class ClinicaPacienteController {
    private final PatientDirectoryService service;
    private final TenantContext tenantContext;

    public ClinicaPacienteController(PatientDirectoryService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public ResponseEntity<PatientPageDto> search(
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String petName,
            @RequestParam(required = false) String microchip,
            @RequestParam(defaultValue = "false") boolean withoutMicrochip,
            @RequestParam(required = false) Species species,
            @RequestParam(required = false) String breed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortable = switch (sort) {
            case "species" -> "species";
            case "breed" -> "breed";
            case "birthDate" -> "birthDate";
            default -> "name";
        };
        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
            Sort.by(sortDirection, sortable).and(Sort.by(Sort.Direction.ASC, "id")));
        return ResponseEntity.ok(service.search(tenantContext.getClinicaId(), ownerName, petName,
            microchip, withoutMicrochip, species, breed, pageable));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientDetailDto> detail(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.detail(tenantContext.getClinicaId(), patientId));
    }
}

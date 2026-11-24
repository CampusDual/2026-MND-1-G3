package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.AnimalDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaAnimalPageDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaFarmSummaryDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaGroupDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaSubtypeDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaFarmPageDto;
import com.campusdual.cd2026mnd1g3.api.dto.AltaGanaderiaAnimalRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.ClasificacionGanaderiaRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.OpcionesClasificacionDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaEstadoMasivoRequestDto;
import com.campusdual.cd2026mnd1g3.api.dto.GanaderiaProtocoloMasivoRequestDto;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario;
import com.campusdual.cd2026mnd1g3.api.enums.GanaderiaGroupType;
import com.campusdual.cd2026mnd1g3.api.service.GanaderiaHierarchyService;
import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.model.entity.FarmEntity;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.AlertaSistemaRepository;
import com.campusdual.cd2026mnd1g3.model.repository.FarmRepository;
import com.campusdual.cd2026mnd1g3.model.repository.GanaderiaHierarchyRepository;
import com.campusdual.cd2026mnd1g3.model.repository.ProtocolEventRepository;
import org.springframework.data.domain.Page;
import com.campusdual.cd2026mnd1g3.model.repository.AnimalRepository;
import com.campusdual.cd2026mnd1g3.api.enums.HealthStatus;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import com.campusdual.cd2026mnd1g3.model.entity.ProtocolEventEntity;
import com.campusdual.cd2026mnd1g3.api.enums.EventStatus;

@Service
@Transactional(readOnly = true)
public class GanaderiaHierarchyServiceImpl implements GanaderiaHierarchyService {
    private final FarmRepository farmRepository;
    private final GanaderiaHierarchyRepository hierarchyRepository;
    private final AlertaSistemaRepository alertRepository;
    private final ProtocolEventRepository protocolRepository;
    private final AnimalRepository animalRepository;

    public GanaderiaHierarchyServiceImpl(FarmRepository farmRepository,
                                         GanaderiaHierarchyRepository hierarchyRepository,
                                         AlertaSistemaRepository alertRepository,
                                         ProtocolEventRepository protocolRepository,
                                         AnimalRepository animalRepository) {
        this.farmRepository = farmRepository;
        this.hierarchyRepository = hierarchyRepository;
        this.alertRepository = alertRepository;
        this.protocolRepository = protocolRepository;
        this.animalRepository = animalRepository;
    }

    @Override
    public GanaderiaFarmPageDto findFarms(Long clinicaId, String search, Pageable pageable) {
        Map<Long, Long> heads = new LinkedHashMap<>();
        for (Object[] row : hierarchyRepository.headsByFarm(clinicaId)) {
            heads.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        Sort sort = pageable.getSort().isUnsorted()
                ? Sort.by(Sort.Direction.ASC, "name")
                : pageable.getSort();
        Pageable deterministic = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(),
                sort.and(Sort.by(Sort.Direction.ASC, "id")));
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        Page<FarmEntity> page = farmRepository.findHierarchyPage(
                clinicaId, normalizedSearch, deterministic);
        return new GanaderiaFarmPageDto(
                page.getContent().stream()
                        .map(farm -> toFarmSummary(clinicaId, farm, heads))
                        .toList(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public List<GanaderiaGroupDto> findGroups(Long clinicaId, Long farmId,
                                              GanaderiaGroupType type) {
        validateFarm(clinicaId, farmId);
        List<Object[]> rows = switch (type) {
            case SUBTIPO -> hierarchyRepository.headsBySubtype(clinicaId, farmId);
            case UBICACION -> hierarchyRepository.headsByLocation(clinicaId, farmId);
            case ESTADO_SANITARIO -> hierarchyRepository.headsByState(clinicaId, farmId);
        };

        long total = rows.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();
        List<GanaderiaGroupDto> groups = new ArrayList<>();
        for (Object[] row : rows) {
            String label = type == GanaderiaGroupType.ESTADO_SANITARIO
                    ? stateLabel(row[0])
                    : String.valueOf(row[0]);
            long heads = ((Number) row[1]).longValue();
            double percentage = total == 0 ? 0 : heads * 100.0 / total;
            groups.add(new GanaderiaGroupDto(
                    label, label, heads, percentage,
                    countAlerts(clinicaId, farmId),
                    countPending(clinicaId, farmId)));
        }
        return groups;
    }

    @Override
    public GanaderiaAnimalPageDto findAnimals(Long clinicaId, Long farmId,
                                              GanaderiaGroupType type, String groupId,
                                              String search, Pageable pageable) {
        validateFarm(clinicaId, farmId);
        Sort sort = pageable.getSort().isUnsorted()
                ? Sort.by(Sort.Direction.ASC, "earTag")
                : pageable.getSort();
        Pageable deterministic = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(),
                sort.and(Sort.by(Sort.Direction.ASC, "id")));

        Page<AnimalEntity> page;
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        if (type == GanaderiaGroupType.ESTADO_SANITARIO) {
            EstadoSanitario state = EstadoSanitario.valueOf(groupId);
            page = hierarchyRepository.findStatePage(
                    clinicaId, farmId, state, normalizedSearch, deterministic);
        } else {
            page = hierarchyRepository.findGroupPage(
                    clinicaId, farmId, type.name(), groupId,
                    normalizedSearch, deterministic);
        }

        return new GanaderiaAnimalPageDto(
                page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional
    public AnimalDto altaAnimal(Long clinicaId, Long farmId,
                                AltaGanaderiaAnimalRequestDto request) {
        FarmEntity farm = validateFarmEntity(clinicaId, farmId);
        if (request == null) {
            throw new IllegalArgumentException("Los datos del animal son obligatorios");
        }
        String crotal = normalizeToken(request.crotal());
        if (crotal.isBlank()) {
            throw new IllegalArgumentException("El crotal o identificador de lote es obligatorio");
        }
        if (animalRepository.existsByFarmIdAndEarTagIgnoreCase(farmId, crotal)) {
            throw new IllegalArgumentException(
                    "Ya existe ese crotal dentro de la explotacion");
        }

        AnimalEntity animal = new AnimalEntity();
        animal.setEarTag(crotal);
        animal.setName(request.nombre() == null ? null : request.nombre().trim());
        animal.setSpecies(request.especie() == null ? farm.getMainSpecies() : request.especie());
        animal.setBirthDate(LocalDate.now());
        animal.setHealthStatus(HealthStatus.HEALTHY);
        animal.setEstadoSanitario(request.estadoSanitario() == null
                ? EstadoSanitario.SANO : request.estadoSanitario());
        animal.setSubtipo(normalizeOptional(request.subtipo()));
        animal.setUbicacion(normalizeOptional(request.ubicacion()));
        animal.setCantidadCabezas(request.cabezas() == null ? 1 : request.cabezas());
        animal.setClinicaId(clinicaId);
        animal.setFarm(farm);
        animal = animalRepository.save(animal);
        return toDto(animal);
    }

    @Override
    @Transactional
    public void clasificarAnimales(Long clinicaId, Long farmId,
                                   ClasificacionGanaderiaRequestDto request) {
        validateFarmEntity(clinicaId, farmId);
        if (request == null || request.animalIds() == null || request.animalIds().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un animal");
        }
        for (Long animalId : request.animalIds()) {
            AnimalEntity animal = animalRepository.findById(animalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Animal", animalId));
            if (!animal.getClinicaId().equals(clinicaId)
                    || !animal.getFarm().getId().equals(farmId)) {
                throw new AccessDeniedException(
                        "El animal no pertenece a la explotacion indicada");
            }
            if (request.subtipo() != null) {
                animal.setSubtipo(normalizeOptional(request.subtipo()));
            }
            if (request.ubicacion() != null) {
                animal.setUbicacion(normalizeOptional(request.ubicacion()));
            }
            if (request.estadoSanitario() != null) {
                animal.setEstadoSanitario(request.estadoSanitario());
            }
            animalRepository.save(animal);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OpcionesClasificacionDto findOpcionesClasificacion(Long clinicaId, Long farmId) {
        FarmEntity farm = validateFarmEntity(clinicaId, farmId);
        LinkedHashSet<String> subtipos = new LinkedHashSet<>();
        subtipos.add("Sin clasificar");
        animalRepository.findDistinctByClinicaIdAndFarmIdAndSubtipoIsNotNull(
                        clinicaId, farmId).stream()
                .map(AnimalEntity::getSubtipo)
                .filter(value -> value != null && !value.isBlank())
                .map(this::normalizeDisplay)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(subtipos::add);

        LinkedHashSet<String> ubicaciones = new LinkedHashSet<>();
        ubicaciones.add("Sin ubicación");
        animalRepository.findDistinctByClinicaIdAndFarmIdAndUbicacionIsNotNull(
                        clinicaId, farmId).stream()
                .map(AnimalEntity::getUbicacion)
                .filter(value -> value != null && !value.isBlank())
                .map(this::normalizeDisplay)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(ubicaciones::add);

        return new OpcionesClasificacionDto(
                List.copyOf(subtipos),
                List.copyOf(ubicaciones),
                recommendedSubtypes(farm.getMainSpecies()),
                recommendedLocations());
    }

    private GanaderiaFarmSummaryDto toFarmSummary(Long clinicaId,
                                                  FarmEntity farm,
                                                  Map<Long, Long> heads) {
        long total = heads.getOrDefault(farm.getId(), 0L);
        List<GanaderiaSubtypeDto> subtypes = hierarchyRepository
                .headsBySubtype(clinicaId, farm.getId()).stream()
                .map(row -> {
                    long count = ((Number) row[1]).longValue();
                    double percentage = total == 0 ? 0 : count * 100.0 / total;
                    String label = String.valueOf(row[0]);
                    return new GanaderiaSubtypeDto(label, label, count, percentage);
                })
                .toList();
        return new GanaderiaFarmSummaryDto(
                farm.getId(), farm.getId(), clinicaId, farm.getName(),
                farm.getRegaCode(), farm.getMainSpecies(), total, subtypes,
                countAlerts(clinicaId, farm.getId()),
                countPending(clinicaId, farm.getId()));
    }

    @Override
    @Transactional
    public int aplicarEstadoMasivo(Long clinicaId, Long farmId,
                                   GanaderiaEstadoMasivoRequestDto request) {
        validateFarmEntity(clinicaId, farmId);
        if (request == null || request.animalIds() == null || request.animalIds().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un animal");
        }
        int updated = 0;
        for (Long animalId : request.animalIds()) {
            AnimalEntity animal = animalRepository.findById(animalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Animal", animalId));
            if (!animal.getClinicaId().equals(clinicaId)
                    || !animal.getFarm().getId().equals(farmId)) {
                throw new AccessDeniedException(
                        "El animal no pertenece a la explotacion indicada");
            }
            animal.setEstadoSanitario(request.estadoSanitario());
            animalRepository.save(animal);
            updated++;
        }
        return updated;
    }

    @Override
    @Transactional
    public int aplicarProtocoloMasivo(Long clinicaId, Long farmId,
                                      GanaderiaProtocoloMasivoRequestDto request) {
        FarmEntity farm = validateFarmEntity(clinicaId, farmId);
        if (request == null || request.animalIds() == null || request.animalIds().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un animal");
        }
        int created = 0;
        for (Long animalId : request.animalIds()) {
            AnimalEntity animal = animalRepository.findById(animalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Animal", animalId));
            if (!animal.getClinicaId().equals(clinicaId)
                    || !animal.getFarm().getId().equals(farmId)) {
                throw new AccessDeniedException(
                        "El animal no pertenece a la explotacion indicada");
            }
            ProtocolEventEntity event = new ProtocolEventEntity();
            event.setEventType(request.eventType());
            event.setTitle(request.title());
            event.setApplicationDate(request.applicationDate());
            event.setWithdrawalDays(request.withdrawalDays());
            event.setWithdrawalEndDate(request.withdrawalEndDate());
            event.setNextCycleDays(request.nextCycleDays() == null ? 0 : request.nextCycleDays());
            event.setStatus(EventStatus.PROGRAMADO);
            event.setAnimal(animal);
            event.setFarm(farm);
            protocolRepository.save(event);
            created++;
        }
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public AnimalDto findByCrotal(Long clinicaId, Long farmId, String crotal) {
        validateFarm(clinicaId, farmId);
        if (crotal == null || crotal.isBlank()) {
            throw new IllegalArgumentException("El crotal es obligatorio");
        }
        AnimalEntity animal = animalRepository.findByClinicaIdAndFarmIdAndEarTagIgnoreCase(
                        clinicaId, farmId, crotal.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Animal", null));
        return toDto(animal);
    }

    private void validateFarm(Long clinicaId, Long farmId) {
        if (farmId == null
                || farmRepository.findById(farmId).stream()
                .noneMatch(farm -> farm.getClinicaId().equals(clinicaId))) {
            throw new ResourceNotFoundException("Explotacion", farmId);
        }
    }

    private FarmEntity validateFarmEntity(Long clinicaId, Long farmId) {
        if (farmId == null) {
            throw new ResourceNotFoundException("Explotacion", null);
        }
        return farmRepository.findById(farmId)
                .filter(farm -> farm.getClinicaId().equals(clinicaId))
                .orElseThrow(() -> new AccessDeniedException(
                        "La explotacion no pertenece a la clinica"));
    }

    private String normalizeToken(String value) {
        if (value == null) return "";
        return value.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()
                || normalized.equalsIgnoreCase("Sin clasificar")
                || normalized.equalsIgnoreCase("Sin ubicación")
                || normalized.equalsIgnoreCase("Sin ubicacion")) {
            return null;
        }
        return normalized.toUpperCase(java.util.Locale.ROOT)
                .replace('Á', 'A')
                .replace('É', 'E')
                .replace('Í', 'I')
                .replace('Ó', 'O')
                .replace('Ú', 'U')
                .replace(' ', '_');
    }

    private String normalizeDisplay(String value) {
        if (value == null) return "";
        String normalized = value.toUpperCase(java.util.Locale.ROOT)
                .replace('_', ' ');
        if (normalized.length() == 1) {
            return normalized;
        }
        return normalized.substring(0, 1)
                + normalized.substring(1).toLowerCase(java.util.Locale.ROOT);
    }

    private List<String> recommendedSubtypes(com.campusdual.cd2026mnd1g3.api.enums.Species species) {
        if (species == com.campusdual.cd2026mnd1g3.api.enums.Species.OVINO
                || species == com.campusdual.cd2026mnd1g3.api.enums.Species.PORCINO) {
            return List.of("MADRES", "CEBO", "TRANSICION", "RECRIA", "TERNEROS", "SECAS");
        }
        return List.of("ORDEÑO", "RECRIA", "TERNEROS", "SECAS", "CEBO", "MADRES", "TRANSICION");
    }

    private List<String> recommendedLocations() {
        return List.of("NAVE_1", "NAVE_2", "CORRAL_A", "PARCELA_NORTE", "ENFERMERIA");
    }

    private long countAlerts(Long clinicaId, Long farmId) {
        return alertRepository.countActiveByFarm(clinicaId, farmId);
    }

    private long countPending(Long clinicaId, Long farmId) {
        return protocolRepository.countPendingByFarm(clinicaId, farmId);
    }

    private boolean containsFolded(String value, String search) {
        return value != null && value.toLowerCase(java.util.Locale.ROOT)
                .contains(search.toLowerCase(java.util.Locale.ROOT).trim());
    }

    private String stateLabel(Object state) {
        if (state == null) return "SANO";
        return switch (state.toString()) {
            case "ENFERMO", "CUARENTENA" -> "Critico";
            case "EN_TRATAMIENTO", "EN_RETIRADA_MEDICAMENTO" -> "En tratamiento";
            case "SANO" -> "SANO";
            default -> state.toString();
        };
    }

    private AnimalDto toDto(AnimalEntity entity) {
        AnimalDto dto = new AnimalDto(
                entity.getId(), entity.getEarTag(), entity.getName(),
                entity.getSpecies(), entity.getBirthDate(), entity.getHealthStatus(),
                entity.getFarm().getId());
        dto.setEstadoSanitario(entity.getEstadoSanitario());
        dto.setFechaFinRetirada(entity.getFechaFinRetirada());
        dto.setNotas(entity.getNotas());
        dto.setSubtipo(entity.getSubtipo());
        dto.setUbicacion(entity.getUbicacion());
        dto.setCantidadCabezas(entity.getCantidadCabezas());
        dto.setClinicaId(entity.getClinicaId());
        return dto;
    }
}

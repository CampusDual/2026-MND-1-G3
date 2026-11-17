package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.FarmDto;
import com.campusdual.cd2026mnd1g3.model.entity.FarmEntity;
import com.campusdual.cd2026mnd1g3.model.repository.FarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;

@Service
@Transactional
public class FarmService {
    private final FarmRepository repository;

    public FarmService(FarmRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<FarmDto> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public FarmDto findById(Long id) {
        return toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Explotacion", id)));
    }

    public FarmDto save(FarmDto dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank() || dto.getRegaCode() == null
                || dto.getMainSpecies() == null || dto.getManagerName() == null || dto.getWhatsappPhone() == null) {
            throw new IllegalArgumentException("Los datos de la explotacion son obligatorios");
        }

        if (repository.existsByRegaCode(dto.getRegaCode().trim())) {
            throw new IllegalArgumentException("Ya existe una explotacion con ese codigo REGA");
        }

        FarmEntity entity = new FarmEntity();
        entity.setName(dto.getName().trim());
        entity.setRegaCode(dto.getRegaCode().trim());
        entity.setMainSpecies(dto.getMainSpecies());
        entity.setManagerName(dto.getManagerName().trim());
        entity.setWhatsappPhone(dto.getWhatsappPhone().trim());
        return toDto(repository.save(entity));
    }

    public FarmDto update(Long id, FarmDto dto) {
        FarmEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Explotacion", id));
        entity.setName(dto.getName());
        entity.setRegaCode(dto.getRegaCode());
        entity.setMainSpecies(dto.getMainSpecies());
        entity.setManagerName(dto.getManagerName());
        entity.setWhatsappPhone(dto.getWhatsappPhone());
        return toDto(repository.save(entity));
    }

    public void delete(Long id) {
        FarmEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Explotacion", id));
        repository.delete(entity);
    }

    private FarmDto toDto(FarmEntity e) {
        FarmDto dto = new FarmDto(e.getId(), e.getName(), e.getRegaCode(), e.getMainSpecies(),
                e.getManagerName(), e.getWhatsappPhone());
        dto.setVeterinarianAssignedPhone(e.getVeterinarianAssignedPhone());
        return dto;
    }
}

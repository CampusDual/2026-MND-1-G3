package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.AnimalDto;
import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.model.entity.FarmEntity;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.AnimalRepository;
import com.campusdual.cd2026mnd1g3.model.repository.FarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class AnimalService {
    private final AnimalRepository animalRepository;
    private final FarmRepository farmRepository;

    public AnimalService(AnimalRepository animalRepository, FarmRepository farmRepository) {
        this.animalRepository = animalRepository;
        this.farmRepository = farmRepository;
    }

    @Transactional(readOnly = true)
    public List<AnimalDto> findAll() {
        return animalRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AnimalDto findById(Long id) {
        return toDto(animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal", id)));
    }

    public AnimalDto save(AnimalDto dto) {
        if (dto == null || dto.getEarTag() == null || dto.getSpecies() == null || dto.getBirthDate() == null
                || dto.getHealthStatus() == null || dto.getFarmId() == null || dto.getEarTag().isBlank()) {
            throw new IllegalArgumentException("Los datos del animal son obligatorios");
        }

        if (animalRepository.existsByEarTag(dto.getEarTag().trim())) {
            throw new IllegalArgumentException("Ya existe un animal con ese crotal");
        }

        FarmEntity farm = farmRepository.findById(dto.getFarmId()).orElseThrow(() -> new ResourceNotFoundException("Explotacion", dto.getFarmId()));
        AnimalEntity entity = new AnimalEntity();
        entity.setEarTag(dto.getEarTag().trim());
        entity.setName(dto.getName());
        entity.setSpecies(dto.getSpecies());
        entity.setBirthDate(dto.getBirthDate());
        entity.setHealthStatus(dto.getHealthStatus());
        entity.setFarm(farm);
        return toDto(animalRepository.save(entity));
    }

    public void delete(Long id) {
        AnimalEntity entity = animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal", id));
        animalRepository.delete(entity);
    }

    private AnimalDto toDto(AnimalEntity e) {
        AnimalDto dto = new AnimalDto(e.getId(), e.getEarTag(), e.getName(), e.getSpecies(),
                e.getBirthDate(), e.getHealthStatus(), e.getFarm().getId());
        dto.setEstadoSanitario(e.getEstadoSanitario());
        dto.setFechaFinRetirada(e.getFechaFinRetirada());
        dto.setNotas(e.getNotas());
        return dto;
    }
}

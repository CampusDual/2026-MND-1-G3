package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.AnimalDto;
import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.model.repository.AnimalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GanaderiaQueryService {
    private final AnimalRepository animalRepository;

    public GanaderiaQueryService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public List<AnimalDto> findAnimalesByClinica(Long clinicaId) {
        return animalRepository.findAll().stream()
                .filter(animal -> animal.getFarm().getClinicaId().equals(clinicaId))
                .map(this::toDto).toList();
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

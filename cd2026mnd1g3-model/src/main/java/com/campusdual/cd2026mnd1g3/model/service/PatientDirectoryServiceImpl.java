package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.PatientDetailDto;
import com.campusdual.cd2026mnd1g3.api.dto.PatientOwnerDto;
import com.campusdual.cd2026mnd1g3.api.dto.PatientPageDto;
import com.campusdual.cd2026mnd1g3.api.dto.PatientSummaryDto;
import com.campusdual.cd2026mnd1g3.api.enums.Species;
import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.model.entity.PatientOwnerEntity;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.AnimalRepository;
import com.campusdual.cd2026mnd1g3.model.specification.PatientDirectorySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "clinicaTransactionManager", readOnly = true)
public class PatientDirectoryServiceImpl implements PatientDirectoryService {
    private final AnimalRepository animalRepository;

    public PatientDirectoryServiceImpl(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    @Override
    public PatientPageDto search(Long clinicaId, String ownerName, String petName, String microchip,
                                 boolean withoutMicrochip, Species species, String breed, Pageable pageable) {
        Specification<AnimalEntity> specification = Specification
            .where(PatientDirectorySpecification.byClinic(clinicaId))
            .and(PatientDirectorySpecification.ownerName(ownerName))
            .and(PatientDirectorySpecification.petName(petName))
            .and(PatientDirectorySpecification.microchip(microchip, withoutMicrochip))
            .and(PatientDirectorySpecification.species(species))
            .and(PatientDirectorySpecification.breed(breed));
        Page<AnimalEntity> page = animalRepository.findAll(specification, pageable);
        PatientPageDto dto = new PatientPageDto();
        dto.setContent(page.getContent().stream().map(this::toSummary).toList());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        return dto;
    }

    @Override
    public PatientDetailDto detail(Long clinicaId, Long patientId) {
        AnimalEntity animal = animalRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente", patientId));
        if (!clinicaId.equals(animal.getClinicaId())) {
            throw new ResourceNotFoundException("Paciente", patientId);
        }
        PatientDetailDto dto = new PatientDetailDto();
        dto.setPatient(toSummary(animal));
        dto.setVaccinationStatus("SIN_DATOS");
        dto.setNextVaccinationDate(null);
        if (animal.getOwner() != null) {
            dto.setOwnerPets(animal.getOwner().getAnimals().stream()
                .filter(ownerPet -> clinicaId.equals(ownerPet.getClinicaId()))
                .map(this::toSummary)
                .toList());
        }
        return dto;
    }

    private PatientSummaryDto toSummary(AnimalEntity entity) {
        PatientSummaryDto dto = new PatientSummaryDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSpecies(entity.getSpecies());
        dto.setBreed(entity.getBreed());
        dto.setMicrochip(entity.getMicrochip());
        dto.setBirthDate(entity.getBirthDate());
        dto.setEstadoSanitario(entity.getEstadoSanitario() == null
            ? entity.getHealthStatus().name()
            : entity.getEstadoSanitario().name());
        if (entity.getOwner() != null) {
            PatientOwnerEntity owner = entity.getOwner();
            PatientOwnerDto ownerDto = new PatientOwnerDto();
            ownerDto.setId(owner.getId());
            ownerDto.setFullName(owner.getFullName());
            ownerDto.setPhoneE164(owner.getPhoneE164());
            ownerDto.setNotificationsEnabled(owner.isNotificationsEnabled());
            dto.setOwner(ownerDto);
        }
        return dto;
    }
}

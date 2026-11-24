package com.campusdual.cd2026mnd1g3;

import com.campusdual.cd2026mnd1g3.api.enums.GanaderiaGroupType;
import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.model.entity.FarmEntity;
import com.campusdual.cd2026mnd1g3.model.repository.AnimalRepository;
import com.campusdual.cd2026mnd1g3.model.repository.FarmRepository;
import com.campusdual.cd2026mnd1g3.model.repository.GanaderiaHierarchyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GanaderiaHierarchyITTest {
    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private GanaderiaHierarchyRepository hierarchyRepository;

    private FarmEntity farmOne;
    private FarmEntity farmTwo;

    @BeforeEach
    void setup() {
        farmOne = new FarmEntity();
        farmOne.setName("Explotacion A");
        farmOne.setRegaCode("TEST-REGA-A");
        farmOne.setMainSpecies(com.campusdual.cd2026mnd1g3.api.enums.Species.VACUNO_LECHE);
        farmOne.setManagerName("Responsable A");
        farmOne.setClinicaId(9901L);
        farmOne = farmRepository.save(farmOne);

        farmTwo = new FarmEntity();
        farmTwo.setName("Explotacion B");
        farmTwo.setRegaCode("TEST-REGA-B");
        farmTwo.setMainSpecies(com.campusdual.cd2026mnd1g3.api.enums.Species.OVINO);
        farmTwo.setManagerName("Responsable B");
        farmTwo.setClinicaId(9901L);
        farmTwo = farmRepository.save(farmTwo);

        for (int index = 0; index < 500; index++) {
            AnimalEntity animal = new AnimalEntity();
            animal.setEarTag("TEST-CROTAL-" + index);
            animal.setName("Animal " + index);
            animal.setSpecies(com.campusdual.cd2026mnd1g3.api.enums.Species.VACUNO_LECHE);
            animal.setBirthDate(LocalDate.of(2020, 1, 1));
            animal.setHealthStatus(com.campusdual.cd2026mnd1g3.api.enums.HealthStatus.HEALTHY);
            animal.setEstadoSanitario(com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario.SANO);
            animal.setClinicaId(9901L);
            animal.setCantidadCabezas(index % 11 == 0 ? 3 : 1);
            animal.setSubtipo(index % 5 == 0 ? null : "Ordeño");
            animal.setUbicacion(index % 7 == 0 ? null : "Nave 1");
            animal.setFarm(farmOne);
            animalRepository.save(animal);
        }

        for (int index = 0; index < 25; index++) {
            AnimalEntity animal = new AnimalEntity();
            animal.setEarTag("TEST-OTHER-" + index);
            animal.setName("Other " + index);
            animal.setSpecies(com.campusdual.cd2026mnd1g3.api.enums.Species.OVINO);
            animal.setBirthDate(LocalDate.of(2021, 1, 1));
            animal.setHealthStatus(com.campusdual.cd2026mnd1g3.api.enums.HealthStatus.HEALTHY);
            animal.setEstadoSanitario(com.campusdual.cd2026mnd1g3.api.enums.EstadoSanitario.SANO);
            animal.setClinicaId(9901L);
            animal.setCantidadCabezas(1);
            animal.setSubtipo("Madres");
            animal.setUbicacion("Corral 2");
            animal.setFarm(farmTwo);
            animalRepository.save(animal);
        }
    }

    @Test
    void aggregateTotalsKeepNullGroupsAndExcludeOtherFarm() {
        long total = hierarchyRepository.headsBySubtype(9901L, farmOne.getId()).stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();
        long otherFarm = hierarchyRepository.headsBySubtype(9901L, farmTwo.getId()).stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        assertThat(total).isGreaterThanOrEqualTo(500L);
        assertThat(otherFarm).isEqualTo(25L);
        assertThat(hierarchyRepository.headsBySubtype(9901L, farmOne.getId()))
                .anyMatch(row -> "Sin clasificar".equals(row[0]));
        assertThat(hierarchyRepository.headsByLocation(9901L, farmOne.getId()))
                .anyMatch(row -> "Sin ubicacion".equals(row[0]));
    }

    @Test
    void paginationIsDeterministicAndIsolated() {
        var first = hierarchyRepository.findGroupPage(
                9901L,
                farmOne.getId(),
                GanaderiaGroupType.SUBTIPO.name(),
                "Ordeño",
                null,
                PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("id").ascending()));

        var second = hierarchyRepository.findGroupPage(
                9901L,
                farmOne.getId(),
                GanaderiaGroupType.SUBTIPO.name(),
                "Ordeño",
                null,
                PageRequest.of(1, 20, org.springframework.data.domain.Sort.by("id").ascending()));

        assertThat(first.getContent()).doesNotContainAnyElementsOf(second.getContent());
        assertThat(first.getContent()).allMatch(animal -> animal.getFarm().getId().equals(farmOne.getId()));
        assertThat(first.getContent()).isSortedAccordingTo(
                (left, right) -> Long.compare(left.getId(), right.getId()));
    }
}

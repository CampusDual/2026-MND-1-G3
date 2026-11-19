package com.campusdual.cd2026mnd1g3.model.specification;

import com.campusdual.cd2026mnd1g3.api.enums.Species;
import com.campusdual.cd2026mnd1g3.model.entity.AnimalEntity;
import com.campusdual.cd2026mnd1g3.model.entity.PatientOwnerEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class PatientDirectorySpecification {
    private PatientDirectorySpecification() {
    }

    public static Specification<AnimalEntity> byClinic(Long clinicaId) {
        return (root, query, cb) -> cb.equal(root.get("clinicaId"), clinicaId);
    }

    public static Specification<AnimalEntity> ownerName(String ownerName) {
        if (ownerName == null || ownerName.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String value = normalize(ownerName);
        return (root, query, cb) -> {
            Join<AnimalEntity, PatientOwnerEntity> owner = root.join("owner", JoinType.LEFT);
            return cb.like(cb.lower(owner.get("normalizedFullName")), "%" + value + "%");
        };
    }

    public static Specification<AnimalEntity> petName(String petName) {
        if (petName == null || petName.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String value = normalize(petName);
        return (root, query, cb) -> cb.like(cb.lower(root.get("normalizedName")), "%" + value + "%");
    }

    public static Specification<AnimalEntity> microchip(String microchip, boolean withoutMicrochip) {
        if (withoutMicrochip) {
            return (root, query, cb) -> cb.or(
                cb.isNull(root.get("microchip")),
                cb.equal(cb.trim(root.get("microchip")), "")
            );
        }
        if (microchip == null || microchip.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String value = microchip.trim();
        return (root, query, cb) -> cb.or(
            cb.equal(root.get("microchip"), value),
            cb.like(root.get("microchip"), "%" + value)
        );
    }

    public static Specification<AnimalEntity> species(Species species) {
        if (species == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("species"), species);
    }

    public static Specification<AnimalEntity> breed(String breed) {
        if (breed == null || breed.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        if ("__UNSPECIFIED__".equals(breed)) {
            return (root, query, cb) -> cb.or(
                cb.isNull(root.get("breed")),
                cb.equal(cb.trim(root.get("breed")), "")
            );
        }
        if ("__MIXED__".equals(breed)) {
            return (root, query, cb) -> cb.or(
                cb.lower(root.get("breed")).in("mestizo", "sin raza definida", "mestizo / sin raza definida"),
                cb.isNull(root.get("breed")),
                cb.equal(cb.trim(root.get("breed")), "")
            );
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("breed")), normalize(breed));
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase()
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ü", "u");
    }
}

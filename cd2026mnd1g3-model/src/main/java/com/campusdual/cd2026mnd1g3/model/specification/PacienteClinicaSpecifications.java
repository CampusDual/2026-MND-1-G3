package com.campusdual.cd2026mnd1g3.model.specification;

import com.campusdual.cd2026mnd1g3.api.enums.EspecieMascota;
import com.campusdual.cd2026mnd1g3.model.entity.PacienteClinicaEntity;
import org.springframework.data.jpa.domain.Specification;

import java.text.Normalizer;
import java.util.Locale;

public final class PacienteClinicaSpecifications {
    private PacienteClinicaSpecifications() {
    }

    public static Specification<PacienteClinicaEntity> forClinica(Long clinicaId) {
        return (root, query, builder) -> builder.equal(root.get("clinicaId"), clinicaId);
    }

    public static Specification<PacienteClinicaEntity> active() {
        return (root, query, builder) -> builder.isTrue(root.get("activo"));
    }

    public static Specification<PacienteClinicaEntity> name(String value) {
        if (value == null || value.isBlank()) {
            return Specification.unrestricted();
        }
        return (root, query, builder) ->
                builder.like(root.get("nombreNormalizado"),
                        "%" + normalize(value) + "%");
    }

    public static Specification<PacienteClinicaEntity> owner(String value) {
        if (value == null || value.isBlank()) {
            return Specification.unrestricted();
        }
        String normalized = normalize(value);
        return (root, query, builder) ->
                builder.like(root.join("propietario").get("nombreNormalizado"),
                        "%" + normalized + "%");
    }

    public static Specification<PacienteClinicaEntity> microchip(String value, Boolean withoutMicrochip) {
        if (Boolean.TRUE.equals(withoutMicrochip)) {
            return (root, query, builder) -> builder.or(
                    builder.isNull(root.get("microchip")),
                    builder.equal(builder.trim(root.get("microchip")), ""));
        }
        if (value == null || value.isBlank()) {
            return Specification.unrestricted();
        }
        String digits = value.replaceAll("[^0-9]", "");
        return (root, query, builder) -> builder.or(
                builder.equal(root.get("microchip"), value.trim()),
                builder.like(root.get("microchip"), "%" + digits));
    }

    public static Specification<PacienteClinicaEntity> species(EspecieMascota species) {
        if (species == null) {
            return Specification.unrestricted();
        }
        return (root, query, builder) -> builder.equal(root.get("especie"), species);
    }

    public static Specification<PacienteClinicaEntity> breed(String breed) {
        if (breed == null || breed.isBlank()) {
            return Specification.unrestricted();
        }
        if ("Sin especificar".equalsIgnoreCase(breed)) {
            return (root, query, builder) -> builder.or(
                    builder.isNull(root.get("raza")),
                    builder.equal(builder.trim(root.get("raza")), ""));
        }
        String normalized = normalize(breed);
        return (root, query, builder) ->
                builder.like(root.get("razaNormalizada"), "%" + normalized + "%");
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return decomposed.toLowerCase(Locale.ROOT).trim();
    }
}

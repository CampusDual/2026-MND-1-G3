package com.campusdual.cd2026mnd1g3.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AltaPropietarioRequestDto(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 120) String apellidos,
        @Size(max = 20) String dniNif,
        @NotBlank @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$") String telefono,
        @Email @Size(max = 160) String email
) {}

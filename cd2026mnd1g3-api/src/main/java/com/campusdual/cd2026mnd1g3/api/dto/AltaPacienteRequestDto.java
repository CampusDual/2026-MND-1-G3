package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.EspecieMascota;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoEsterilizacion;
import com.campusdual.cd2026mnd1g3.api.enums.SexoMascota;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AltaPacienteRequestDto(
        Long clienteExistenteId,
        @Valid AltaPropietarioRequestDto nuevoCliente,
        @NotBlank @Size(max = 100) String nombre,
        @NotNull EspecieMascota especie,
        @Size(max = 100) String raza,
        @Size(max = 30) String microchip,
        boolean sinMicrochip,
        LocalDate fechaNacimiento,
        boolean fechaNacimientoEstimada,
        @Min(0) @Max(50) Integer edadAproxAnos,
        @Min(0) @Max(11) Integer edadAproxMeses,
        @NotNull SexoMascota sexo,
        @NotNull EstadoEsterilizacion esterilizado,
        @DecimalMin("0.01") Double pesoInicial,
        boolean alergias,
        boolean manejoNervioso,
        boolean patologiaCronica,
        @Size(max = 500) String notaBanderas
) {}

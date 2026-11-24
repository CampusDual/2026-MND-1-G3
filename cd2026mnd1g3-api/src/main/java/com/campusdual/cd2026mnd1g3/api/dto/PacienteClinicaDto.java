package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.EspecieMascota;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoPaciente;
import com.campusdual.cd2026mnd1g3.api.enums.SexoMascota;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoEsterilizacion;

import java.time.LocalDate;

public record PacienteClinicaDto(
        Long id,
        Long clinicaId,
        Long propietarioId,
        String nombre,
        EspecieMascota especie,
        String raza,
        String microchip,
        LocalDate fechaNacimiento,
        boolean fechaNacimientoEstimada,
        Integer edadAproxAnos,
        Integer edadAproxMeses,
        SexoMascota sexo,
        EstadoEsterilizacion esterilizado,
        Double pesoInicial,
        String unidadPeso,
        boolean alergias,
        boolean manejoNervioso,
        boolean patologiaCronica,
        String notaBanderas,
        EstadoPaciente estadoPaciente,
        PropietarioClinicaDto propietario
) {}

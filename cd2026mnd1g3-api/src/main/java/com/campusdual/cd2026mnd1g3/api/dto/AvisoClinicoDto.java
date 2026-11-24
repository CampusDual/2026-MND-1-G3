package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.CategoriaAvisoClinico;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoAvisoClinico;
import com.campusdual.cd2026mnd1g3.api.enums.MotivoDescarteAviso;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AvisoClinicoDto(
        Long id,
        Long clinicaId,
        Long pacienteId,
        Long propietarioId,
        String nombrePaciente,
        String nombrePropietario,
        String telefonoPropietario,
        String emailPropietario,
        CategoriaAvisoClinico categoria,
        String titulo,
        String detalleClinico,
        LocalDate fechaObjetivo,
        EstadoAvisoClinico estado,
        int intentos,
        LocalDateTime ultimoIntento,
        String ultimoUsuario,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String progreso,
        MotivoDescarteAviso motivoDescarte,
        boolean activo
) {}

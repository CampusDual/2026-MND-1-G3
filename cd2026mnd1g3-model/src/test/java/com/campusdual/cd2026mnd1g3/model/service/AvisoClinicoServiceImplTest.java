package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.CambioEstadoAvisoDto;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoAvisoClinico;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoPaciente;
import com.campusdual.cd2026mnd1g3.api.enums.MotivoDescarteAviso;
import com.campusdual.cd2026mnd1g3.model.entity.AvisoClinicoEntity;
import com.campusdual.cd2026mnd1g3.model.entity.PacienteClinicaEntity;
import com.campusdual.cd2026mnd1g3.model.entity.PropietarioClinicaEntity;
import com.campusdual.cd2026mnd1g3.model.repository.AvisoClinicoRepository;
import com.campusdual.cd2026mnd1g3.model.repository.PacienteClinicaRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AvisoClinicoServiceImplTest {
    private final AvisoClinicoRepository avisoRepository = mock(AvisoClinicoRepository.class);
    private final PacienteClinicaRepository pacienteRepository = mock(PacienteClinicaRepository.class);
    private final AvisoClinicoServiceImpl service =
            new AvisoClinicoServiceImpl(avisoRepository, pacienteRepository);

    @Test
    void fallecimientoRequiereDobleConfirmacion() {
        AvisoClinicoEntity aviso = avisoFixture();
        when(avisoRepository.findByIdAndClinicaId(10L, 1L)).thenReturn(Optional.of(aviso));

        CambioEstadoAvisoDto request = new CambioEstadoAvisoDto(
                EstadoAvisoClinico.DESCARTADA,
                null,
                MotivoDescarteAviso.FALLECIMIENTO_BAJA,
                null,
                false);

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(1L, 10L, request, "recepcion"));
    }

    @Test
    void fallecimientoConfirmaDaDeBajaYCancelaFuturos() {
        AvisoClinicoEntity aviso = avisoFixture();
        when(avisoRepository.findByIdAndClinicaId(10L, 1L)).thenReturn(Optional.of(aviso));
        when(avisoRepository.save(aviso)).thenReturn(aviso);

        CambioEstadoAvisoDto request = new CambioEstadoAvisoDto(
                EstadoAvisoClinico.DESCARTADA,
                null,
                MotivoDescarteAviso.FALLECIMIENTO_BAJA,
                null,
                true);

        service.cambiarEstado(1L, 10L, request, "recepcion");

        assertEquals(EstadoPaciente.INACTIVO, aviso.getPaciente().getEstadoPaciente());
        assertFalse(aviso.getPaciente().isActivo());
        assertFalse(aviso.isActivo());
        verify(avisoRepository).cancelarPendientesPorBaja(eq(1L), eq(20L), anyList());
    }

    private AvisoClinicoEntity avisoFixture() {
        PropietarioClinicaEntity owner = new PropietarioClinicaEntity();
        owner.setId(30L);
        owner.setNombre("Ana");
        owner.setApellidos("Garcia");
        owner.setTelefono("+34600123456");

        PacienteClinicaEntity patient = new PacienteClinicaEntity();
        patient.setId(20L);
        patient.setClinicaId(1L);
        patient.setNombre("Luna");
        patient.setPropietario(owner);
        patient.setActivo(true);
        patient.setEstadoPaciente(EstadoPaciente.ACTIVO);

        AvisoClinicoEntity aviso = new AvisoClinicoEntity();
        aviso.setId(10L);
        aviso.setClinicaId(1L);
        aviso.setPaciente(patient);
        aviso.setPropietario(owner);
        aviso.setActivo(true);
        aviso.setEstado(EstadoAvisoClinico.PENDIENTE);
        return aviso;
    }
}

package com.campusdual.cd2026mnd1g3;

import com.campusdual.cd2026mnd1g3.api.enums.*;
import com.campusdual.cd2026mnd1g3.model.entity.*;
import com.campusdual.cd2026mnd1g3.model.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Carga datos iniciales y de demostración para la clínica veterinaria.
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private final FarmRepository farmRepository;
    private final AnimalRepository animalRepository;
    private final ProtocolEventRepository eventRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final LoteStockRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AlertaSistemaRepository alertaRepository;

    public DataInitializer(FarmRepository farmRepository,
                           AnimalRepository animalRepository,
                           ProtocolEventRepository eventRepository,
                           MedicamentoRepository medicamentoRepository,
                           LoteStockRepository loteRepository,
                           MovimientoInventarioRepository movimientoRepository,
                           AlertaSistemaRepository alertaRepository) {
        this.farmRepository = farmRepository;
        this.animalRepository = animalRepository;
        this.eventRepository = eventRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.alertaRepository = alertaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (farmRepository.count() > 0) {
            return;
        }

        FarmEntity dairyFarm = createFarm("Ganadería O Carballo S.L.", "ES150300001234",
                Species.VACUNO_LECHE, "Manuel Rodríguez");
        FarmEntity pigFarm = createFarm("Granja A Ponte", "ES360150005678",
                Species.PORCINO, "José Martínez");

        AnimalEntity healthy = createAnimal("ES2800000001-001", "Bruma", Species.VACUNO_LECHE,
                HealthStatus.HEALTHY, EstadoSanitario.SANO, dairyFarm, null);
        AnimalEntity withdrawal = createAnimal("ES2800000001-002", "Niebla", Species.VACUNO_LECHE,
                HealthStatus.SICK, EstadoSanitario.EN_RETIRADA_MEDICAMENTO, dairyFarm,
                LocalDate.now().plusDays(5));
        AnimalEntity sick = createAnimal("ES2800000002-001", "Pino", Species.PORCINO,
                HealthStatus.SICK, EstadoSanitario.ENFERMO, pigFarm, null);
        createAnimal("ES2800000002-002", "Roble", Species.PORCINO,
                HealthStatus.QUARANTINE, EstadoSanitario.CUARENTENA, pigFarm, null);
        createAnimal("ES011234567890", "Lúa", Species.VACUNO_LECHE,
                HealthStatus.HEALTHY, EstadoSanitario.SANO, dairyFarm, null);
        createAnimal("ES011234567891", "Sombra", Species.OVINO,
                HealthStatus.SICK, EstadoSanitario.EN_TRATAMIENTO, pigFarm, null);

        createEvent(EventType.VACUNACION, "Revacunación anual",
                LocalDate.now(), 0, 30, EventStatus.APLICADO, healthy, dairyFarm);
        createEvent(EventType.TRATAMIENTO_MEDICO, "Tratamiento antibiótico",
                LocalDate.now().minusDays(1), 5, 0,
                EventStatus.APLICADO, withdrawal, dairyFarm);
        createEvent(EventType.REVISION_PERIODICA, "Revisión veterinaria",
                LocalDate.now().plusDays(1), 0, 0,
                EventStatus.PROGRAMADO, sick, pigFarm);

        LoteStockEntity loteAmoxi = createMedicamentoYLote(
                "Amoxicilina Bovina LA", "Amoxicilina", "VACUNO/PORCINO",
                CategoriaPran.B, 5, 20, "LOT-AMX-001");
        LoteStockEntity loteVacuna = createMedicamentoYLote(
                "Vacuna IBR/BVD", "Antígenos IBR/BVD", "VACUNO",
                CategoriaPran.C, 0, 10, "LOT-IBR-001");
        LoteStockEntity loteAntip = createMedicamentoYLote(
                "Ivermectina Inyectable", "Ivermectina", "VACUNO/PORCINO",
                CategoriaPran.B, 14, 15, "LOT-IVM-001");

        createEntrada(loteAmoxi, 50);
        createEntrada(loteVacuna, 30);
        createEntrada(loteAntip, 40);
        createSalida(loteAmoxi, 3.5, sick);

        createAlerta(DestinatarioRol.VETERINARIAN, TipoAlerta.URGENCIA_ANIMAL_ENFERMO,
                "Animal enfermo", "Pino ha sido informado como enfermo.", pigFarm.getId(), sick.getId());
        createAlerta(DestinatarioRol.FARM_MANAGER, TipoAlerta.FIN_RETIRADA_MEDICAMENTO,
                "Fin de retirada próximo", "Niebla finaliza su periodo de retirada el "
                        + LocalDate.now().plusDays(5) + ".", dairyFarm.getId(), withdrawal.getId());
        createAlerta(DestinatarioRol.CLINIC_ADMIN, TipoAlerta.RECORDATORIO_CICLICO,
                "Recordatorio cíclico", "Revacunación anual de Bruma programada.", dairyFarm.getId(),
                healthy.getId());
    }

    private FarmEntity createFarm(String name, String regaCode, Species species, String manager) {
        FarmEntity farm = new FarmEntity();
        farm.setName(name);
        farm.setRegaCode(regaCode);
        farm.setMainSpecies(species);
        farm.setManagerName(manager);
        farm.setClinicaId(1L);
        return farmRepository.save(farm);
    }

    private AnimalEntity createAnimal(String earTag, String name, Species species,
                                      HealthStatus healthStatus, EstadoSanitario sanitaryStatus,
                                      FarmEntity farm, LocalDate withdrawalEnd) {
        AnimalEntity animal = new AnimalEntity();
        animal.setEarTag(earTag);
        animal.setName(name);
        animal.setSpecies(species);
        animal.setBirthDate(LocalDate.of(2022, 1, 15));
        animal.setHealthStatus(healthStatus);
        animal.setEstadoSanitario(sanitaryStatus);
        animal.setFechaFinRetirada(withdrawalEnd);
        animal.setFarm(farm);
        return animalRepository.save(animal);
    }

    private ProtocolEventEntity createEvent(EventType type, String title,
                                            LocalDate applicationDate, int withdrawalDays,
                                            int nextCycleDays, EventStatus status,
                                            AnimalEntity animal, FarmEntity farm) {
        ProtocolEventEntity event = new ProtocolEventEntity();
        event.setEventType(type);
        event.setTitle(title);
        event.setApplicationDate(applicationDate);
        event.setWithdrawalDays(withdrawalDays);
        event.setWithdrawalEndDate(withdrawalDays > 0
                ? applicationDate.plusDays(withdrawalDays) : null);
        event.setNextCycleDays(nextCycleDays);
        event.setStatus(status);
        event.setAnimal(animal);
        event.setFarm(farm);
        return eventRepository.save(event);
    }

    private LoteStockEntity createMedicamentoYLote(String nombre, String principio,
                                                   String especie, CategoriaPran categoria,
                                                   int retiro, double stockMinimo, String lote) {
        MedicamentoEntity medicamento = new MedicamentoEntity();
        medicamento.setClinicaId(1L);
        medicamento.setNombreComercial(nombre);
        medicamento.setPrincipioActivo(principio);
        medicamento.setEspecieDestino(especie);
        medicamento.setCategoriaPran(categoria);
        medicamento.setDiasRetiradaDefecto(retiro);
        medicamento.setStockMinimoAlerta(stockMinimo);
        MedicamentoEntity saved = medicamentoRepository.save(medicamento);

        LoteStockEntity loteStock = new LoteStockEntity();
        loteStock.setMedicamento(saved);
        loteStock.setNumeroLote(lote);
        loteStock.setFechaCaducidad(LocalDate.now().plusMonths(12));
        loteStock.setProveedor("Proveedor veterinario inicial");
        return loteRepository.save(loteStock);
    }

    private void createEntrada(LoteStockEntity lote, double cantidad) {
        MovimientoInventarioEntity movimiento = new MovimientoInventarioEntity();
        movimiento.setClinicaId(1L);
        movimiento.setLoteStock(lote);
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
        movimiento.setCantidad(cantidad);
        movimiento.setFechaHora(LocalDateTime.now());
        movimientoRepository.save(movimiento);
    }

    private void createSalida(LoteStockEntity lote, double cantidad, AnimalEntity animal) {
        MovimientoInventarioEntity movimiento = new MovimientoInventarioEntity();
        movimiento.setClinicaId(1L);
        movimiento.setLoteStock(lote);
        movimiento.setTipoMovimiento(TipoMovimiento.SALIDA_GANADERIA);
        movimiento.setCantidad(-cantidad);
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setAnimalId(animal.getId());
        movimiento.setExplotacionId(animal.getFarm().getId());
        movimientoRepository.save(movimiento);
    }

    private void createAlerta(DestinatarioRol rol, TipoAlerta tipo,
                              String titulo, String mensaje, Long explotacionId, Long animalId) {
        AlertaSistemaEntity alerta = new AlertaSistemaEntity();
        alerta.setClinicaId(1L);
        alerta.setDestinatarioRol(rol);
        alerta.setTipoAlerta(tipo);
        alerta.setTitulo(titulo);
        alerta.setMensaje(mensaje);
        alerta.setFechaProgramada(LocalDateTime.now());
        alerta.setExplotacionId(explotacionId);
        alerta.setAnimalId(animalId);
        alertaRepository.save(alerta);
    }
}

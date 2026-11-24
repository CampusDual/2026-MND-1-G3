package com.campusdual.cd2026mnd1g3;

import com.campusdual.cd2026mnd1g3.api.enums.*;
import com.campusdual.cd2026mnd1g3.model.entity.*;
import com.campusdual.cd2026mnd1g3.model.repository.*;
import com.campusdual.cd2026mnd1g3.model.specification.PacienteClinicaSpecifications;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private final PropietarioClinicaRepository propietarioRepository;
    private final PacienteClinicaRepository pacienteRepository;
    private final ConsultaClinicaRepository consultaRepository;
    private final RecetaClinicaRepository recetaRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(FarmRepository farmRepository,
                           AnimalRepository animalRepository,
                           ProtocolEventRepository eventRepository,
                           MedicamentoRepository medicamentoRepository,
                           LoteStockRepository loteRepository,
                           MovimientoInventarioRepository movimientoRepository,
                           AlertaSistemaRepository alertaRepository,
                           PropietarioClinicaRepository propietarioRepository,
                           PacienteClinicaRepository pacienteRepository,
                           ConsultaClinicaRepository consultaRepository,
                           RecetaClinicaRepository recetaRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.farmRepository = farmRepository;
        this.animalRepository = animalRepository;
        this.eventRepository = eventRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.alertaRepository = alertaRepository;
        this.propietarioRepository = propietarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
        this.recetaRepository = recetaRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureAdminUser();
        if (farmRepository.count() == 0) {
            FarmEntity dairyFarm = createFarm("Ganadería El Roble", "ES2800000001",
                    Species.VACUNO_LECHE, "Laura García");
            FarmEntity pigFarm = createFarm("Granja Los Pinos", "ES2800000002",
                    Species.PORCINO, "Miguel López");

        AnimalEntity healthy = createAnimal("ES2800000001-001", "Bruma", Species.VACUNO_LECHE,
                HealthStatus.HEALTHY, EstadoSanitario.SANO, dairyFarm, null);
        AnimalEntity withdrawal = createAnimal("ES2800000001-002", "Niebla", Species.VACUNO_LECHE,
                HealthStatus.SICK, EstadoSanitario.EN_RETIRADA_MEDICAMENTO, dairyFarm,
                LocalDate.now().plusDays(5));
        AnimalEntity sick = createAnimal("ES2800000002-001", "Pino", Species.PORCINO,
                HealthStatus.SICK, EstadoSanitario.ENFERMO, pigFarm, null);
        createAnimal("ES2800000002-002", "Roble", Species.PORCINO,
                HealthStatus.QUARANTINE, EstadoSanitario.CUARENTENA, pigFarm, null);

        createEvent(EventType.VACUNACION, "Revacunación anual",
                LocalDate.now(), 0, 30, EventStatus.APLICADO, healthy, dairyFarm);
        createEvent(EventType.TRATAMIENTO_MEDICO, "Tratamiento antibiótico",
                LocalDate.now().minusDays(1), 5, 0,
                EventStatus.APLICADO, withdrawal, dairyFarm);
        createEvent(EventType.REVISION_PERIODICA, "Revisión veterinaria",
                LocalDate.now().plusDays(1), 0, 0,
                EventStatus.PROGRAMADO, sick, pigFarm);

        LoteStockEntity loteAmoxi = createMedicamentoYLote(
                "Amoxicilina 15%", "Amoxicilina", "VACUNO/PORCINO",
                CategoriaPran.B, 5, 20, "LOT-AMX-001");
        LoteStockEntity loteVacuna = createMedicamentoYLote(
                "Vacuna BTV", "Virus vivo atenuado", "VACUNO",
                CategoriaPran.C, 0, 10, "LOT-BTV-001");
        LoteStockEntity loteAntip = createMedicamentoYLote(
                "Ivermectina 1%", "Ivermectina", "VACUNO/PORCINO",
                CategoriaPran.B, 14, 15, "LOT-IVM-001");

        createEntrada(loteAmoxi, 50);
        createEntrada(loteVacuna, 30);
        createEntrada(loteAntip, 40);

        createAlerta(DestinatarioRol.VETERINARIAN, TipoAlerta.URGENCIA_ANIMAL_ENFERMO,
                "Animal enfermo", "Pino ha sido informado como enfermo.", pigFarm.getId(), sick.getId());
        createAlerta(DestinatarioRol.FARM_MANAGER, TipoAlerta.FIN_RETIRADA_MEDICAMENTO,
                "Fin de retirada próximo", "Niebla finaliza su periodo de retirada el "
                        + LocalDate.now().plusDays(5) + ".", dairyFarm.getId(), withdrawal.getId());
            createAlerta(DestinatarioRol.CLINIC_ADMIN, TipoAlerta.RECORDATORIO_CICLICO,
                    "Recordatorio cíclico", "Revacunación anual de Bruma programada.", dairyFarm.getId(),
                    healthy.getId());
        }
        seedClinicData();
    }

    private void ensureAdminUser() {
        UserEntity admin = userRepository.findByUsername("admin")
                .orElseGet(UserEntity::new);
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin1234"));
        admin.setEnabled(true);
        admin.setClinicaId(1L);

        RoleEntity role = roleRepository.findByName("ROLE_CLINIC_ADMIN")
                .orElseGet(() -> {
                    RoleEntity newRole = new RoleEntity();
                    newRole.setName("ROLE_CLINIC_ADMIN");
                    return roleRepository.save(newRole);
                });
        admin.getRoles().add(role);
        userRepository.save(admin);
    }

    private void seedClinicData() {
        seedOwnerAndPets(
                "José", "García López", "35412890A", "+34611223344", "jose.garcia@email.com",
                new SeedPet("Toby", EspecieMascota.PERRO, "Labrador Retriever",
                        "941000024511001", SexoMascota.MACHO, EstadoEsterilizacion.SI,
                        29.5, LocalDate.of(2021, 3, 10), false, false, false),
                new SeedPet("Kira", EspecieMascota.PERRO, "Border Collie",
                        "941000024511002", SexoMascota.HEMBRA, EstadoEsterilizacion.SI,
                        18.2, LocalDate.of(2022, 7, 14), false, false, false),
                new SeedPet("Bigotes", EspecieMascota.GATO, "Común Europeo",
                        null, SexoMascota.MACHO, EstadoEsterilizacion.SI,
                        4.8, LocalDate.of(2020, 11, 8), false, false, false));

        seedOwnerAndPets(
                "María", "Fernández Soto", "44821903B", "+34622334455", "maria.fernandez@email.com",
                new SeedPet("Nube", EspecieMascota.GATO, "Persa",
                        "941000024511003", SexoMascota.HEMBRA, EstadoEsterilizacion.SI,
                        3.9, LocalDate.of(2023, 2, 20), false, true, false),
                new SeedPet("Simba", EspecieMascota.GATO, "Maine Coon",
                        "941000024511004", SexoMascota.MACHO, EstadoEsterilizacion.NO,
                        6.4, LocalDate.of(2022, 5, 5), false, false, false));

        seedOwnerAndPets(
                "Carlos", "Rodríguez Vázquez", "36190482C", "+34633445566", "carlos.rv@email.com",
                new SeedPet("Rocky", EspecieMascota.PERRO, "Bulldog Francés",
                        null, SexoMascota.MACHO, EstadoEsterilizacion.NO,
                        6.1, LocalDate.of(2023, 8, 12), true, false, false),
                new SeedPet("Thor", EspecieMascota.PERRO, "Pastor Alemán",
                        "941000024511005", SexoMascota.MACHO, EstadoEsterilizacion.NO,
                        34.0, LocalDate.of(2021, 1, 18), false, false, false));

        seedOwnerAndPets(
                "Ana", "Pérez Domínguez", "53194028D", "+34644556677", "ana.perez@email.com",
                new SeedPet("Kiko", EspecieMascota.PAJARO, "Ninfa (Carolina)",
                        null, SexoMascota.MACHO, EstadoEsterilizacion.DESCONOCIDO,
                        95.0, LocalDate.of(2024, 4, 2), false, false, false),
                new SeedPet("Pipo", EspecieMascota.PAJARO, "Agapornis",
                        null, SexoMascota.DESCONOCIDO, EstadoEsterilizacion.DESCONOCIDO,
                        52.0, LocalDate.of(2024, 6, 15), false, false, false));

        seedOwnerAndPets(
                "David", "Martínez Castro", "76891234E", "+34655667788", "david.martinez@email.com",
                new SeedPet("Tambor", EspecieMascota.OTRO, "Conejo Belier",
                        null, SexoMascota.MACHO, EstadoEsterilizacion.SI,
                        1850.0, LocalDate.of(2023, 10, 1), false, false, false),
                new SeedPet("Manchas", EspecieMascota.OTRO, "Cobaya",
                        null, SexoMascota.HEMBRA, EstadoEsterilizacion.NO,
                        920.0, LocalDate.of(2024, 1, 22), false, false, false));

        seedOwnerAndPets(
                "Laura", "Gómez Iglesias", "33458912F", "+34666778899", "laura.gomez@email.com",
                new SeedPet("Canela", EspecieMascota.PERRO, "Mestizo / Sin raza definida",
                        "941000024511006", SexoMascota.HEMBRA, EstadoEsterilizacion.SI,
                        14.5, LocalDate.of(2020, 9, 30), false, false, true));
        seedConsultasEjemplo();
    }

    private void seedConsultasEjemplo() {
        PacienteClinicaEntity toby = pacienteRepository
                .findByClinicaIdAndNombreIgnoreCaseAndActivoTrue(1L, "Toby")
                .orElse(null);
        if (toby == null) return;
        if (consultaRepository.findByClinicaIdOrderByFechaConsultaDesc(1L).stream()
                .anyMatch(consulta -> consulta.getPaciente().getId().equals(toby.getId()))) {
            return;
        }
        ConsultaClinicaEntity consulta = new ConsultaClinicaEntity();
        consulta.setClinicaId(1L);
        consulta.setPaciente(toby);
        consulta.setPropietario(toby.getPropietario());
        consulta.setFechaConsulta(LocalDateTime.now().minusDays(2));
        consulta.setMotivo("Revisión preventiva");
        consulta.setNotas("Consulta inicial de ejemplo vinculada a paciente clínico.");
        consulta.setProximaVacuna(LocalDate.now().plusMonths(6));
        ConsultaClinicaEntity saved = consultaRepository.save(consulta);

        RecetaClinicaEntity receta = new RecetaClinicaEntity();
        receta.setClinicaId(1L);
        receta.setConsulta(saved);
        receta.setPaciente(toby);
        receta.setIndicaciones("Administrar según indicación veterinaria.");
        receta.setFechaEmision(LocalDate.now());
        recetaRepository.save(receta);
    }

    private void seedOwnerAndPets(String nombre, String apellidos, String dni,
                                  String telefono, String email, SeedPet... pets) {
        PropietarioClinicaEntity owner =
                propietarioRepository.findByClinicaIdAndDniNifAndActivoTrue(1L, dni)
                        .orElseGet(() -> propietarioRepository
                                .findByClinicaIdAndTelefonoAndActivoTrue(1L, telefono)
                                .orElseGet(() -> createOwner(nombre, apellidos, dni, telefono, email)));

        for (SeedPet pet : pets) {
            if (pet.microchip() != null
                    && pacienteRepository.findByClinicaIdAndMicrochip(1L, pet.microchip()).isPresent()) {
                continue;
            }
            if (pet.microchip() == null
                    && pacienteRepository.existsByClinicaIdAndPropietarioIdAndNombreNormalizadoAndEspecie(
                    1L, owner.getId(),
                    PacienteClinicaSpecifications.normalize(pet.nombre()),
                    pet.especie())) {
                continue;
            }

            PacienteClinicaEntity entity = new PacienteClinicaEntity();
            entity.setClinicaId(1L);
            entity.setPropietario(owner);
            entity.setNombre(pet.nombre());
            entity.setNombreNormalizado(PacienteClinicaSpecifications.normalize(pet.nombre()));
            entity.setEspecie(pet.especie());
            entity.setRaza(pet.raza());
            entity.setRazaNormalizada(PacienteClinicaSpecifications.normalize(pet.raza()));
            entity.setMicrochip(pet.microchip());
            entity.setFechaNacimiento(pet.fechaNacimiento());
            entity.setFechaNacimientoEstimada(false);
            entity.setSexo(pet.sexo());
            entity.setEsterilizado(pet.esterilizado());
            entity.setPesoInicial(pet.peso());
            entity.setUnidadPeso(pet.especie() == EspecieMascota.PERRO
                    || pet.especie() == EspecieMascota.GATO ? "kg" : "g");
            entity.setAlergias(pet.alergias());
            entity.setManejoNervioso(pet.manejoNervioso());
            entity.setPatologiaCronica(pet.patologiaCronica());
            entity.setEstadoPaciente(EstadoPaciente.ACTIVO);
            entity.setActivo(true);
            pacienteRepository.save(entity);
        }
    }

    private PropietarioClinicaEntity createOwner(String nombre, String apellidos,
                                                String dni, String telefono, String email) {
        PropietarioClinicaEntity owner = new PropietarioClinicaEntity();
        owner.setClinicaId(1L);
        owner.setNombre(nombre);
        owner.setApellidos(apellidos);
        owner.setDniNif(dni);
        owner.setTelefono(telefono);
        owner.setEmail(email);
        owner.setNombreNormalizado(PacienteClinicaSpecifications.normalize(nombre + " " + apellidos));
        owner.setNotificacionesInternas(true);
        owner.setActivo(true);
        return propietarioRepository.save(owner);
    }

    private record SeedPet(
            String nombre,
            EspecieMascota especie,
            String raza,
            String microchip,
            SexoMascota sexo,
            EstadoEsterilizacion esterilizado,
            double peso,
            LocalDate fechaNacimiento,
            boolean alergias,
            boolean manejoNervioso,
            boolean patologiaCronica) {}

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

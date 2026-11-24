package com.campusdual.cd2026mnd1g3.model.service;

import com.campusdual.cd2026mnd1g3.api.dto.*;
import com.campusdual.cd2026mnd1g3.api.enums.EspecieMascota;
import com.campusdual.cd2026mnd1g3.api.enums.EstadoPaciente;
import com.campusdual.cd2026mnd1g3.api.service.ClinicaPacienteService;
import com.campusdual.cd2026mnd1g3.model.entity.PacienteClinicaEntity;
import com.campusdual.cd2026mnd1g3.model.entity.PropietarioClinicaEntity;
import com.campusdual.cd2026mnd1g3.model.exception.ResourceNotFoundException;
import com.campusdual.cd2026mnd1g3.model.repository.PacienteClinicaRepository;
import com.campusdual.cd2026mnd1g3.model.repository.PropietarioClinicaRepository;
import com.campusdual.cd2026mnd1g3.model.specification.PacienteClinicaSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClinicaPacienteServiceImpl implements ClinicaPacienteService {
    private final PacienteClinicaRepository pacienteRepository;
    private final PropietarioClinicaRepository propietarioRepository;

    public ClinicaPacienteServiceImpl(PacienteClinicaRepository pacienteRepository,
                                      PropietarioClinicaRepository propietarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.propietarioRepository = propietarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<PacienteClinicaDto> findPacientes(
            Long clinicaId, String propietario, String nombre, String microchip,
            Boolean sinMicrochip, EspecieMascota especie, String raza, Pageable pageable) {
        Specification<PacienteClinicaEntity> specification =
                Specification.where(PacienteClinicaSpecifications.forClinica(clinicaId))
                        .and(PacienteClinicaSpecifications.active())
                        .and(PacienteClinicaSpecifications.owner(propietario))
                        .and(PacienteClinicaSpecifications.name(nombre))
                        .and(PacienteClinicaSpecifications.microchip(microchip, sinMicrochip))
                        .and(PacienteClinicaSpecifications.species(especie))
                        .and(PacienteClinicaSpecifications.breed(raza));

        Sort sort = pageable.getSort().isUnsorted()
                ? Sort.by(Sort.Direction.ASC, "nombre")
                : pageable.getSort();
        Pageable deterministic = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(),
                sort.and(Sort.by(Sort.Direction.ASC, "id")));
        Page<PacienteClinicaEntity> page = pacienteRepository.findAll(specification, deterministic);
        return new PageResponseDto<>(
                page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteClinicaDto findPaciente(Long clinicaId, Long pacienteId) {
        return pacienteRepository.findByIdAndClinicaIdAndActivoTrue(pacienteId, clinicaId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", pacienteId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropietarioClinicaDto> buscarPropietarios(Long clinicaId, String termino) {
        if (termino == null || termino.isBlank()) {
            return List.of();
        }
        String normalized = PacienteClinicaSpecifications.normalize(termino);
        return propietarioRepository.searchByTerm(clinicaId, normalized)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PropietarioClinicaDto buscarDuplicadoPropietario(
            Long clinicaId, String telefono, String dniNif) {
        Optional<PropietarioClinicaEntity> match = Optional.empty();
        if (telefono != null && !telefono.isBlank()) {
            match = propietarioRepository.findByClinicaIdAndTelefonoAndActivoTrue(clinicaId, telefono);
        }
        if (match.isEmpty() && dniNif != null && !dniNif.isBlank()) {
            match = propietarioRepository.findByClinicaIdAndDniNifAndActivoTrue(
                    clinicaId, dniNif.trim().toUpperCase());
        }
        return match.map(this::toDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteClinicaDto> findMascotasPropietario(Long clinicaId, Long propietarioId) {
        return pacienteRepository.findByClinicaIdAndPropietarioIdAndActivoTrue(
                        clinicaId, propietarioId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findRazas(Long clinicaId, EspecieMascota especie) {
        LinkedHashSet<String> razas = new LinkedHashSet<>();
        List<String> standard = switch (especie) {
            case PERRO -> List.of(
                    "Mestizo / Sin raza definida", "Sin especificar",
                    "Akita Inu", "Beagle", "Bichón Maltés", "Border Collie", "Bóxer",
                    "Bulldog Francés", "Bulldog Inglés", "Caniche", "Carlino (Pug)",
                    "Chihuahua", "Cocker Spaniel", "Dálmata", "Doberman",
                    "Galgo Español", "Golden Retriever", "Husky Siberiano",
                    "Jack Russell Terrier", "Labrador Retriever", "Mastín Español",
                    "Pastor Alemán", "Pastor Belga Malinois", "Perro de Agua Español",
                    "Pinscher Miniatura", "Pit Bull Terrier", "Podenco", "Pomerania",
                    "Rottweiler", "San Bernardo", "Schnauzer Miniatura",
                    "Setter Inglés", "Shih Tzu", "Teckel (Perro Salchicha)",
                    "West Highland White Terrier", "Yorkshire Terrier");
            case GATO -> List.of(
                    "Mestizo / Sin raza definida", "Sin especificar",
                    "Abisinio", "Azul Ruso", "Bengalí", "Bosque de Noruega",
                    "British Shorthair", "Común Europeo", "Exótico de Pelo Corto",
                    "Maine Coon", "Persa", "Ragdoll", "Sagrado de Birmania",
                    "Scottish Fold", "Siamés", "Sphynx (Esfinge)");
            case PAJARO -> List.of(
                    "Sin especificar", "Agapornis", "Cacatúa", "Canario",
                    "Cotorra Argentina", "Diamante Mandarín", "Guacamayo",
                    "Jilguero", "Loro Amazona", "Loro Yaco (Gris Africano)",
                    "Ninfa (Carolina)", "Periquito Común");
            case OTRO -> List.of(
                    "Sin especificar", "Chinchilla", "Cobaya", "Conejo Belier",
                    "Conejo Enano / Toy", "Erizo Africano", "Gecko Leopardo",
                    "Hámster", "Hurón", "Iguana", "Petauro del Azúcar",
                    "Pogona (Dragón Barbudo)", "Rata Doméstica",
                    "Tortuga de Agua", "Tortuga de Tierra");
            case null -> List.of("Mestizo / Sin raza definida", "Sin especificar");
        };
        razas.addAll(standard);
        if (especie != null) {
            pacienteRepository.findByClinicaIdAndEspecieAndRazaIsNotNullAndActivoTrue(
                            clinicaId, especie).stream()
                    .map(PacienteClinicaEntity::getRaza)
                    .filter(raza -> raza != null && !raza.isBlank())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(razas::add);
        }
        return List.copyOf(razas);
    }

    @Override
    public AltaPacienteResponseDto altaPaciente(Long clinicaId, AltaPacienteRequestDto request) {
        validateRequest(request);

        PropietarioClinicaEntity owner;
        if (request.clienteExistenteId() != null) {
            owner = propietarioRepository.findByIdAndClinicaIdAndActivoTrue(
                            request.clienteExistenteId(), clinicaId)
                    .orElseThrow(() -> new AccessDeniedException(
                            "El propietario no pertenece a la clinica"));
        } else {
            AltaPropietarioRequestDto newOwner = request.nuevoCliente();
            if (newOwner == null) {
                throw new IllegalArgumentException("Debe indicar un cliente existente o uno nuevo");
            }
            propietarioRepository.findByClinicaIdAndTelefonoAndActivoTrue(
                            clinicaId, newOwner.telefono())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                                "Ya existe un cliente con ese telefono");
                    });
            if (newOwner.dniNif() != null && !newOwner.dniNif().isBlank()) {
                String dni = newOwner.dniNif().trim().toUpperCase();
                propietarioRepository.findByClinicaIdAndDniNifAndActivoTrue(
                                clinicaId, dni)
                        .ifPresent(existing -> {
                            throw new IllegalArgumentException(
                                    "Ya existe un cliente con ese DNI/NIF");
                        });
            }
            owner = new PropietarioClinicaEntity();
            owner.setClinicaId(clinicaId);
            owner.setNombre(newOwner.nombre().trim());
            owner.setApellidos(newOwner.apellidos().trim());
            owner.setDniNif(newOwner.dniNif() == null
                    ? null : newOwner.dniNif().trim().toUpperCase());
            owner.setTelefono(newOwner.telefono().trim());
            owner.setEmail(newOwner.email() == null ? null : newOwner.email().trim());
            owner.setNombreNormalizado(PacienteClinicaSpecifications.normalize(
                    owner.getNombre() + " " + owner.getApellidos()));
            owner = propietarioRepository.save(owner);
        }

        if (!request.sinMicrochip() && request.microchip() != null
                && !request.microchip().isBlank()) {
            String microchip = request.microchip().trim();
            pacienteRepository.findByClinicaIdAndMicrochip(clinicaId, microchip)
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                                "Ya existe un paciente con ese microchip");
                    });
        }

        PacienteClinicaEntity patient = new PacienteClinicaEntity();
        patient.setClinicaId(clinicaId);
        patient.setPropietario(owner);
        patient.setNombre(request.nombre().trim());
        patient.setNombreNormalizado(PacienteClinicaSpecifications.normalize(request.nombre()));
        patient.setEspecie(request.especie());
        patient.setRaza(normalizeBreed(request.raza()));
        patient.setRazaNormalizada(request.raza() == null
                ? null : PacienteClinicaSpecifications.normalize(request.raza()));
        patient.setMicrochip(request.sinMicrochip()
                ? null : request.microchip() == null ? null : request.microchip().trim());
        patient.setFechaNacimiento(request.fechaNacimiento());
        patient.setFechaNacimientoEstimada(request.fechaNacimientoEstimada());
        patient.setEdadAproxAnos(request.edadAproxAnos());
        patient.setEdadAproxMeses(request.edadAproxMeses());
        patient.setSexo(request.sexo());
        patient.setEsterilizado(request.esterilizado());
        patient.setPesoInicial(request.pesoInicial());
        patient.setUnidadPeso(weightUnit(request.especie()));
        patient.setAlergias(request.alergias());
        patient.setManejoNervioso(request.manejoNervioso());
        patient.setPatologiaCronica(request.patologiaCronica());
        patient.setNotaBanderas(request.notaBanderas());
        patient.setEstadoPaciente(EstadoPaciente.ACTIVO);
        patient = pacienteRepository.save(patient);

        return new AltaPacienteResponseDto(toDto(patient), toDto(owner));
    }

    private void validateRequest(AltaPacienteRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de alta es obligatoria");
        }
        if (request.clienteExistenteId() == null
                && (request.nuevoCliente() == null)) {
            throw new IllegalArgumentException("Debe indicar un propietario");
        }
        if (request.clienteExistenteId() != null
                && request.nuevoCliente() != null) {
            throw new IllegalArgumentException(
                    "No puede indicar propietario existente y nuevo simultaneamente");
        }
        if (!request.fechaNacimientoEstimada() && request.fechaNacimiento() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar fecha exacta o activar edad aproximada");
        }
        if (request.fechaNacimientoEstimada()
                && (request.edadAproxAnos() == null && request.edadAproxMeses() == null)) {
            throw new IllegalArgumentException(
                    "Debe indicar años o meses aproximados");
        }
    }

    private String normalizeBreed(String breed) {
        if (breed == null || breed.isBlank()
                || "Sin especificar".equalsIgnoreCase(breed.trim())) {
            return null;
        }
        return breed.trim();
    }

    private String weightUnit(EspecieMascota species) {
        return species == EspecieMascota.PERRO || species == EspecieMascota.GATO
                ? "kg" : "g";
    }

    private PacienteClinicaDto toDto(PacienteClinicaEntity patient) {
        return new PacienteClinicaDto(
                patient.getId(), patient.getClinicaId(), patient.getPropietario().getId(),
                patient.getNombre(), patient.getEspecie(), patient.getRaza(),
                patient.getMicrochip(), patient.getFechaNacimiento(),
                patient.isFechaNacimientoEstimada(), patient.getEdadAproxAnos(),
                patient.getEdadAproxMeses(), patient.getSexo(), patient.getEsterilizado(),
                patient.getPesoInicial(), patient.getUnidadPeso(), patient.isAlergias(),
                patient.isManejoNervioso(), patient.isPatologiaCronica(),
                patient.getNotaBanderas(), patient.getEstadoPaciente(),
                toDto(patient.getPropietario()));
    }

    private PropietarioClinicaDto toDto(PropietarioClinicaEntity owner) {
        return new PropietarioClinicaDto(
                owner.getId(), owner.getNombre(), owner.getApellidos(),
                owner.getDniNif(), owner.getTelefono(), owner.getEmail(),
                owner.isNotificacionesInternas(),
                pacienteRepository.countByClinicaIdAndPropietarioIdAndActivoTrue(
                        owner.getClinicaId(), owner.getId()));
    }
}

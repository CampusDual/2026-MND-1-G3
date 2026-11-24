package com.campusdual.cd2026mnd1g3.model.entity;

import com.campusdual.cd2026mnd1g3.api.enums.*;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "pacientes_mascotas")
public class PacienteClinicaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "clinica_id", nullable = false)
    private Long clinicaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietario_id", nullable = false)
    private PropietarioClinicaEntity propietario;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(name = "nombre_normalizado", nullable = false, length = 100)
    private String nombreNormalizado;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EspecieMascota especie;
    @Column(length = 100)
    private String raza;
    @Column(name = "raza_normalizada", length = 100)
    private String razaNormalizada;
    @Column(length = 30)
    private String microchip;
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    @Column(name = "fecha_nacimiento_estimada", nullable = false)
    private boolean fechaNacimientoEstimada;
    @Column(name = "edad_aprox_anos")
    private Integer edadAproxAnos;
    @Column(name = "edad_aprox_meses")
    private Integer edadAproxMeses;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SexoMascota sexo = SexoMascota.DESCONOCIDO;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEsterilizacion esterilizado = EstadoEsterilizacion.DESCONOCIDO;
    @Column(name = "peso_inicial")
    private Double pesoInicial;
    @Column(name = "unidad_peso", length = 2)
    private String unidadPeso;
    @Column(nullable = false)
    private boolean alergias;
    @Column(name = "manejo_nervioso", nullable = false)
    private boolean manejoNervioso;
    @Column(name = "patologia_cronica", nullable = false)
    private boolean patologiaCronica;
    @Column(name = "nota_banderas", length = 500)
    private String notaBanderas;
    @Column(nullable = false)
    private boolean activo = true;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_paciente", nullable = false, length = 20)
    private EstadoPaciente estadoPaciente = EstadoPaciente.ACTIVO;
    @Column(length = 1000)
    private String notas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
    public PropietarioClinicaEntity getPropietario() { return propietario; }
    public void setPropietario(PropietarioClinicaEntity propietario) { this.propietario = propietario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNombreNormalizado() { return nombreNormalizado; }
    public void setNombreNormalizado(String nombreNormalizado) { this.nombreNormalizado = nombreNormalizado; }
    public EspecieMascota getEspecie() { return especie; }
    public void setEspecie(EspecieMascota especie) { this.especie = especie; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getRazaNormalizada() { return razaNormalizada; }
    public void setRazaNormalizada(String razaNormalizada) { this.razaNormalizada = razaNormalizada; }
    public String getMicrochip() { return microchip; }
    public void setMicrochip(String microchip) { this.microchip = microchip; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public boolean isFechaNacimientoEstimada() { return fechaNacimientoEstimada; }
    public void setFechaNacimientoEstimada(boolean fechaNacimientoEstimada) { this.fechaNacimientoEstimada = fechaNacimientoEstimada; }
    public Integer getEdadAproxAnos() { return edadAproxAnos; }
    public void setEdadAproxAnos(Integer edadAproxAnos) { this.edadAproxAnos = edadAproxAnos; }
    public Integer getEdadAproxMeses() { return edadAproxMeses; }
    public void setEdadAproxMeses(Integer edadAproxMeses) { this.edadAproxMeses = edadAproxMeses; }
    public SexoMascota getSexo() { return sexo; }
    public void setSexo(SexoMascota sexo) { this.sexo = sexo; }
    public EstadoEsterilizacion getEsterilizado() { return esterilizado; }
    public void setEsterilizado(EstadoEsterilizacion esterilizado) { this.esterilizado = esterilizado; }
    public Double getPesoInicial() { return pesoInicial; }
    public void setPesoInicial(Double pesoInicial) { this.pesoInicial = pesoInicial; }
    public String getUnidadPeso() { return unidadPeso; }
    public void setUnidadPeso(String unidadPeso) { this.unidadPeso = unidadPeso; }
    public boolean isAlergias() { return alergias; }
    public void setAlergias(boolean alergias) { this.alergias = alergias; }
    public boolean isManejoNervioso() { return manejoNervioso; }
    public void setManejoNervioso(boolean manejoNervioso) { this.manejoNervioso = manejoNervioso; }
    public boolean isPatologiaCronica() { return patologiaCronica; }
    public void setPatologiaCronica(boolean patologiaCronica) { this.patologiaCronica = patologiaCronica; }
    public String getNotaBanderas() { return notaBanderas; }
    public void setNotaBanderas(String notaBanderas) { this.notaBanderas = notaBanderas; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public EstadoPaciente getEstadoPaciente() { return estadoPaciente; }
    public void setEstadoPaciente(EstadoPaciente estadoPaciente) { this.estadoPaciente = estadoPaciente; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}

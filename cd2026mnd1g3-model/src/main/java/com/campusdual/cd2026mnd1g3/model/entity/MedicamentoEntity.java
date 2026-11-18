package com.campusdual.cd2026mnd1g3.model.entity;

import com.campusdual.cd2026mnd1g3.api.enums.CategoriaPran;
import jakarta.persistence.*;

@Entity
@Table(name = "medicamentos")
public class MedicamentoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "clinica_id", nullable = false)
    private Long clinicaId;
    @Column(name = "nombre_comercial", nullable = false, length = 150)
    private String nombreComercial;
    @Column(name = "principio_activo", nullable = false, length = 150)
    private String principioActivo;
    @Column(name = "especie_destino", nullable = false, length = 30)
    private String especieDestino;
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_pran", nullable = false, length = 5)
    private CategoriaPran categoriaPran;
    @Column(name = "dias_retirada_defecto", nullable = false)
    private int diasRetiradaDefecto;
    @Column(name = "stock_minimo_alerta", nullable = false)
    private double stockMinimoAlerta;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }
    public String getPrincipioActivo() { return principioActivo; }
    public void setPrincipioActivo(String principioActivo) { this.principioActivo = principioActivo; }
    public String getEspecieDestino() { return especieDestino; }
    public void setEspecieDestino(String especieDestino) { this.especieDestino = especieDestino; }
    public CategoriaPran getCategoriaPran() { return categoriaPran; }
    public void setCategoriaPran(CategoriaPran categoriaPran) { this.categoriaPran = categoriaPran; }
    public int getDiasRetiradaDefecto() { return diasRetiradaDefecto; }
    public void setDiasRetiradaDefecto(int diasRetiradaDefecto) { this.diasRetiradaDefecto = diasRetiradaDefecto; }
    public double getStockMinimoAlerta() { return stockMinimoAlerta; }
    public void setStockMinimoAlerta(double stockMinimoAlerta) { this.stockMinimoAlerta = stockMinimoAlerta; }
}

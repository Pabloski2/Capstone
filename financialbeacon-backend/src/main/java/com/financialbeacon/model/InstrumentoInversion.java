package com.financialbeacon.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "instrumentos_inversion")
public class InstrumentoInversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaInversion categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NivelRiesgo riesgo;

    @Column(nullable = false)
    private BigDecimal rentabilidadMin;

    @Column(nullable = false)
    private BigDecimal rentabilidadMax;

    @Column(nullable = false)
    private Integer plazoMinimoMeses;

    @Column(nullable = false)
    private BigDecimal montoMinimo;

    @Column(nullable = false, length = 120)
    private String liquidez;

    @Column(nullable = false, length = 255)
    private String dondeInvertir;

    @Column(nullable = false, length = 600)
    private String descripcion;

    @Column(nullable = false, length = 800)
    private String consideraciones;

    @Column(nullable = false, length = 400)
    private String fuente;

    @Column(nullable = false)
    private LocalDate fechaReferencia;

    public InstrumentoInversion() {}

    public InstrumentoInversion(Long id, String nombre, CategoriaInversion categoria, NivelRiesgo riesgo, BigDecimal rentabilidadMin, BigDecimal rentabilidadMax, Integer plazoMinimoMeses, BigDecimal montoMinimo, String liquidez, String dondeInvertir, String descripcion, String consideraciones, String fuente, LocalDate fechaReferencia) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.riesgo = riesgo;
        this.rentabilidadMin = rentabilidadMin;
        this.rentabilidadMax = rentabilidadMax;
        this.plazoMinimoMeses = plazoMinimoMeses;
        this.montoMinimo = montoMinimo;
        this.liquidez = liquidez;
        this.dondeInvertir = dondeInvertir;
        this.descripcion = descripcion;
        this.consideraciones = consideraciones;
        this.fuente = fuente;
        this.fechaReferencia = fechaReferencia;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public CategoriaInversion getCategoria() { return categoria; }
    public void setCategoria(CategoriaInversion categoria) { this.categoria = categoria; }

    public NivelRiesgo getRiesgo() { return riesgo; }
    public void setRiesgo(NivelRiesgo riesgo) { this.riesgo = riesgo; }

    public BigDecimal getRentabilidadMin() { return rentabilidadMin; }
    public void setRentabilidadMin(BigDecimal rentabilidadMin) { this.rentabilidadMin = rentabilidadMin; }

    public BigDecimal getRentabilidadMax() { return rentabilidadMax; }
    public void setRentabilidadMax(BigDecimal rentabilidadMax) { this.rentabilidadMax = rentabilidadMax; }

    public Integer getPlazoMinimoMeses() { return plazoMinimoMeses; }
    public void setPlazoMinimoMeses(Integer plazoMinimoMeses) { this.plazoMinimoMeses = plazoMinimoMeses; }

    public BigDecimal getMontoMinimo() { return montoMinimo; }
    public void setMontoMinimo(BigDecimal montoMinimo) { this.montoMinimo = montoMinimo; }

    public String getLiquidez() { return liquidez; }
    public void setLiquidez(String liquidez) { this.liquidez = liquidez; }

    public String getDondeInvertir() { return dondeInvertir; }
    public void setDondeInvertir(String dondeInvertir) { this.dondeInvertir = dondeInvertir; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getConsideraciones() { return consideraciones; }
    public void setConsideraciones(String consideraciones) { this.consideraciones = consideraciones; }

    public String getFuente() { return fuente; }
    public void setFuente(String fuente) { this.fuente = fuente; }

    public LocalDate getFechaReferencia() { return fechaReferencia; }
    public void setFechaReferencia(LocalDate fechaReferencia) { this.fechaReferencia = fechaReferencia; }
}

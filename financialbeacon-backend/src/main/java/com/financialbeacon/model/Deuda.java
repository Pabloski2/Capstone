package com.financialbeacon.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "deudas")
public class Deuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private BigDecimal montoTotal;

    @Column(nullable = false)
    private BigDecimal tasaInteres;

    @Column(nullable = false)
    private BigDecimal cuotaMinima;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Deuda() {}

    public Deuda(Long id, String nombre, BigDecimal montoTotal, BigDecimal tasaInteres, BigDecimal cuotaMinima, User user) {
        this.id = id;
        this.nombre = nombre;
        this.montoTotal = montoTotal;
        this.tasaInteres = tasaInteres;
        this.cuotaMinima = cuotaMinima;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }

    public BigDecimal getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(BigDecimal tasaInteres) { this.tasaInteres = tasaInteres; }

    public BigDecimal getCuotaMinima() { return cuotaMinima; }
    public void setCuotaMinima(BigDecimal cuotaMinima) { this.cuotaMinima = cuotaMinima; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

package com.financialbeacon.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ingresos_mensuales", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "mes", "anio"})
})
public class IngresoMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public IngresoMensual() {}

    public IngresoMensual(Long id, BigDecimal monto, Integer mes, Integer anio, User user) {
        this.id = id;
        this.monto = monto;
        this.mes = mes;
        this.anio = anio;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

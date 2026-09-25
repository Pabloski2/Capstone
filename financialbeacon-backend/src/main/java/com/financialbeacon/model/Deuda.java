package com.financialbeacon.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "deudas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Deuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private BigDecimal montoTotal;

    @Column(nullable = false)
    private BigDecimal tasaInteres; // porcentaje anual, ej: 24.5

    @Column(nullable = false)
    private BigDecimal cuotaMinima; // pago mínimo mensual exigido

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

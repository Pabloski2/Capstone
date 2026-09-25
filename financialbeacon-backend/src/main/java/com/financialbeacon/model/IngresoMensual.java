package com.financialbeacon.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ingresos_mensuales", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "mes", "anio"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngresoMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private Integer mes; // 1-12

    @Column(nullable = false)
    private Integer anio;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

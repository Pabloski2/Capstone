package com.financialbeacon.dto;

import com.financialbeacon.model.Categoria;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinanzasRequests {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class IngresoRequest {
        private Long userId;
        private BigDecimal monto;
        private Integer mes;
        private Integer anio;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class GastoRequest {
        private Long userId;
        private String descripcion;
        private BigDecimal monto;
        private Categoria categoria;
        private LocalDate fecha;
    }
}

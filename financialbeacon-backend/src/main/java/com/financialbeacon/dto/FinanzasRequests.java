package com.financialbeacon.dto;

import com.financialbeacon.model.Categoria;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinanzasRequests {

    public static class IngresoRequest {

        private Long userId;

        private BigDecimal monto;

        private Integer mes;

        private Integer anio;

        public IngresoRequest() {}

        public IngresoRequest(Long userId, BigDecimal monto, Integer mes, Integer anio) {
            this.userId = userId;
            this.monto = monto;
            this.mes = mes;
            this.anio = anio;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }

        public Integer getMes() { return mes; }
        public void setMes(Integer mes) { this.mes = mes; }

        public Integer getAnio() { return anio; }
        public void setAnio(Integer anio) { this.anio = anio; }
    }

    public static class GastoRequest {

        private Long userId;

        private String descripcion;

        private BigDecimal monto;

        private Categoria categoria;

        private LocalDate fecha;

        public GastoRequest() {}

        public GastoRequest(Long userId, String descripcion, BigDecimal monto, Categoria categoria, LocalDate fecha) {
            this.userId = userId;
            this.descripcion = descripcion;
            this.monto = monto;
            this.categoria = categoria;
            this.fecha = fecha;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }

        public Categoria getCategoria() { return categoria; }
        public void setCategoria(Categoria categoria) { this.categoria = categoria; }

        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    }
}

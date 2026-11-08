package com.financialbeacon.dto;

import com.financialbeacon.model.Categoria;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** El usuario ya no viaja en el cuerpo: se obtiene del token de sesión. */
public class FinanzasRequests {

    public static class IngresoRequest {

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0", message = "El monto no puede ser negativo")
        @Digits(integer = 12, fraction = 2, message = "El monto no es válido")
        private BigDecimal monto;

        @NotNull(message = "El mes es obligatorio")
        @Min(value = 1, message = "El mes debe estar entre 1 y 12")
        @Max(value = 12, message = "El mes debe estar entre 1 y 12")
        private Integer mes;

        @NotNull(message = "El año es obligatorio")
        @Min(value = 2000, message = "El año no es válido")
        @Max(value = 2100, message = "El año no es válido")
        private Integer anio;

        public IngresoRequest() {}

        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }

        public Integer getMes() { return mes; }
        public void setMes(Integer mes) { this.mes = mes; }

        public Integer getAnio() { return anio; }
        public void setAnio(Integer anio) { this.anio = anio; }
    }

    public static class GastoRequest {

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 255, message = "La descripción es demasiado larga")
        private String descripcion;

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
        @Digits(integer = 12, fraction = 2, message = "El monto no es válido")
        private BigDecimal monto;

        @NotNull(message = "La categoría es obligatoria")
        private Categoria categoria;

        @NotNull(message = "La fecha es obligatoria")
        private LocalDate fecha;

        public GastoRequest() {}

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

package com.financialbeacon.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class DeudaDTOs {

    /** El usuario ya no viaja en el cuerpo: se obtiene del token de sesión. */
    public static class DeudaRequest {

        @NotBlank(message = "El nombre de la deuda es obligatorio")
        @Size(max = 255, message = "El nombre es demasiado largo")
        private String nombre;

        @NotNull(message = "El monto total es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto total debe ser mayor que cero")
        @Digits(integer = 12, fraction = 2, message = "El monto total no es válido")
        private BigDecimal montoTotal;

        @NotNull(message = "La tasa de interés es obligatoria")
        @DecimalMin(value = "0", message = "La tasa de interés no puede ser negativa")
        @DecimalMax(value = "1000", message = "La tasa de interés no es válida")
        private BigDecimal tasaInteres;

        @NotNull(message = "La cuota mínima es obligatoria")
        @DecimalMin(value = "0", message = "La cuota mínima no puede ser negativa")
        @Digits(integer = 12, fraction = 2, message = "La cuota mínima no es válida")
        private BigDecimal cuotaMinima;

        public DeudaRequest() {}

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public BigDecimal getMontoTotal() { return montoTotal; }
        public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }

        public BigDecimal getTasaInteres() { return tasaInteres; }
        public void setTasaInteres(BigDecimal tasaInteres) { this.tasaInteres = tasaInteres; }

        public BigDecimal getCuotaMinima() { return cuotaMinima; }
        public void setCuotaMinima(BigDecimal cuotaMinima) { this.cuotaMinima = cuotaMinima; }
    }

    public static class DeudaConPagoDTO {

        private Long deudaId;

        private String nombre;

        private BigDecimal montoTotal;

        private BigDecimal tasaInteres;

        private BigDecimal pagoSugeridoEsteMes;

        public DeudaConPagoDTO() {}

        public DeudaConPagoDTO(Long deudaId, String nombre, BigDecimal montoTotal, BigDecimal tasaInteres, BigDecimal pagoSugeridoEsteMes) {
            this.deudaId = deudaId;
            this.nombre = nombre;
            this.montoTotal = montoTotal;
            this.tasaInteres = tasaInteres;
            this.pagoSugeridoEsteMes = pagoSugeridoEsteMes;
        }

        public Long getDeudaId() { return deudaId; }
        public void setDeudaId(Long deudaId) { this.deudaId = deudaId; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public BigDecimal getMontoTotal() { return montoTotal; }
        public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }

        public BigDecimal getTasaInteres() { return tasaInteres; }
        public void setTasaInteres(BigDecimal tasaInteres) { this.tasaInteres = tasaInteres; }

        public BigDecimal getPagoSugeridoEsteMes() { return pagoSugeridoEsteMes; }
        public void setPagoSugeridoEsteMes(BigDecimal pagoSugeridoEsteMes) { this.pagoSugeridoEsteMes = pagoSugeridoEsteMes; }
    }

    public static class EstrategiaDeudaDTO {

        private String estrategiaRecomendada;

        private String justificacion;

        private BigDecimal montoDisponibleParaDeudas;

        private List<DeudaConPagoDTO> ordenDePago;

        public EstrategiaDeudaDTO() {}

        public EstrategiaDeudaDTO(String estrategiaRecomendada, String justificacion, BigDecimal montoDisponibleParaDeudas, List<DeudaConPagoDTO> ordenDePago) {
            this.estrategiaRecomendada = estrategiaRecomendada;
            this.justificacion = justificacion;
            this.montoDisponibleParaDeudas = montoDisponibleParaDeudas;
            this.ordenDePago = ordenDePago;
        }

        public String getEstrategiaRecomendada() { return estrategiaRecomendada; }
        public void setEstrategiaRecomendada(String estrategiaRecomendada) { this.estrategiaRecomendada = estrategiaRecomendada; }

        public String getJustificacion() { return justificacion; }
        public void setJustificacion(String justificacion) { this.justificacion = justificacion; }

        public BigDecimal getMontoDisponibleParaDeudas() { return montoDisponibleParaDeudas; }
        public void setMontoDisponibleParaDeudas(BigDecimal montoDisponibleParaDeudas) { this.montoDisponibleParaDeudas = montoDisponibleParaDeudas; }

        public List<DeudaConPagoDTO> getOrdenDePago() { return ordenDePago; }
        public void setOrdenDePago(List<DeudaConPagoDTO> ordenDePago) { this.ordenDePago = ordenDePago; }
    }
}

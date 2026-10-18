package com.financialbeacon.dto;

import java.math.BigDecimal;
import java.util.List;

public class DeudaDTOs {

    public static class DeudaRequest {

        private Long userId;

        private String nombre;

        private BigDecimal montoTotal;

        private BigDecimal tasaInteres;

        private BigDecimal cuotaMinima;

        public DeudaRequest() {}

        public DeudaRequest(Long userId, String nombre, BigDecimal montoTotal, BigDecimal tasaInteres, BigDecimal cuotaMinima) {
            this.userId = userId;
            this.nombre = nombre;
            this.montoTotal = montoTotal;
            this.tasaInteres = tasaInteres;
            this.cuotaMinima = cuotaMinima;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

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

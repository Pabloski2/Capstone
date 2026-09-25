package com.financialbeacon.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class DeudaDTOs {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class DeudaRequest {
        private Long userId;
        private String nombre;
        private BigDecimal montoTotal;
        private BigDecimal tasaInteres;
        private BigDecimal cuotaMinima;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class DeudaConPagoDTO {
        private Long deudaId;
        private String nombre;
        private BigDecimal montoTotal;
        private BigDecimal tasaInteres;
        private BigDecimal pagoSugeridoEsteMes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class EstrategiaDeudaDTO {
        private String estrategiaRecomendada; // "AVALANCHA" o "BOLA_DE_NIEVE"
        private String justificacion;
        private BigDecimal montoDisponibleParaDeudas; // = ahorroEstimado del mes
        private List<DeudaConPagoDTO> ordenDePago;
    }
}

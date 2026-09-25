package com.financialbeacon.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenMensualDTO {

    private BigDecimal ingresoMensual;
    private BigDecimal totalGastosFijos;
    private BigDecimal totalGastosVariables;
    private BigDecimal totalGastosHormiga;
    private BigDecimal totalGastos;
    private BigDecimal ahorroEstimado; // ingreso - totalGastos
}

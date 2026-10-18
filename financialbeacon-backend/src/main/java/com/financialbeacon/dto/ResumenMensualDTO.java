package com.financialbeacon.dto;

import java.math.BigDecimal;

public class ResumenMensualDTO {

    private BigDecimal ingresoMensual;

    private BigDecimal totalGastosFijos;

    private BigDecimal totalGastosVariables;

    private BigDecimal totalGastosHormiga;

    private BigDecimal totalGastos;

    private BigDecimal ahorroEstimado;

    public ResumenMensualDTO() {}

    public ResumenMensualDTO(BigDecimal ingresoMensual, BigDecimal totalGastosFijos, BigDecimal totalGastosVariables, BigDecimal totalGastosHormiga, BigDecimal totalGastos, BigDecimal ahorroEstimado) {
        this.ingresoMensual = ingresoMensual;
        this.totalGastosFijos = totalGastosFijos;
        this.totalGastosVariables = totalGastosVariables;
        this.totalGastosHormiga = totalGastosHormiga;
        this.totalGastos = totalGastos;
        this.ahorroEstimado = ahorroEstimado;
    }

    public BigDecimal getIngresoMensual() { return ingresoMensual; }
    public void setIngresoMensual(BigDecimal ingresoMensual) { this.ingresoMensual = ingresoMensual; }

    public BigDecimal getTotalGastosFijos() { return totalGastosFijos; }
    public void setTotalGastosFijos(BigDecimal totalGastosFijos) { this.totalGastosFijos = totalGastosFijos; }

    public BigDecimal getTotalGastosVariables() { return totalGastosVariables; }
    public void setTotalGastosVariables(BigDecimal totalGastosVariables) { this.totalGastosVariables = totalGastosVariables; }

    public BigDecimal getTotalGastosHormiga() { return totalGastosHormiga; }
    public void setTotalGastosHormiga(BigDecimal totalGastosHormiga) { this.totalGastosHormiga = totalGastosHormiga; }

    public BigDecimal getTotalGastos() { return totalGastos; }
    public void setTotalGastos(BigDecimal totalGastos) { this.totalGastos = totalGastos; }

    public BigDecimal getAhorroEstimado() { return ahorroEstimado; }
    public void setAhorroEstimado(BigDecimal ahorroEstimado) { this.ahorroEstimado = ahorroEstimado; }
}

package com.financialbeacon.service;

import com.financialbeacon.dto.DeudaDTOs.EstrategiaDeudaDTO;
import com.financialbeacon.dto.RecomendacionDTO;
import com.financialbeacon.dto.ResumenMensualDTO;
import com.financialbeacon.recomendaciones.MotorRecomendaciones;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.DeudaInfo;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.Entrada;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.GastoInfo;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.PagoSugerido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reúne los datos del usuario (resumen del mes, gastos, deudas y estrategia de pago)
 * y se los entrega al motor de reglas, que genera los mensajes.
 */
@Service
public class RecomendacionesService {

    @Autowired
    private FinanzasService finanzasService;

    @Autowired
    private DebtStrategyService debtStrategyService;

    public List<RecomendacionDTO> generarRecomendaciones(Long userId, Integer mes, Integer anio) {
        return generarRecomendaciones(userId, mes, anio, null);
    }

    /** metaAhorro es opcional: si viene, se agrega un plan para alcanzarla. */
    public List<RecomendacionDTO> generarRecomendaciones(Long userId, Integer mes, Integer anio, BigDecimal metaAhorro) {
        ResumenMensualDTO resumen = finanzasService.obtenerResumenMensual(userId, mes, anio);

        List<GastoInfo> gastos = finanzasService.listarGastosDelMes(userId, mes, anio).stream()
                .map(g -> new GastoInfo(g.getDescripcion(), g.getMonto().doubleValue(), g.getCategoria().name()))
                .toList();

        List<DeudaInfo> deudas = debtStrategyService.listarDeudas(userId).stream()
                .map(d -> new DeudaInfo(d.getNombre(), d.getMontoTotal().doubleValue(),
                        d.getTasaInteres().doubleValue(), d.getCuotaMinima().doubleValue()))
                .toList();

        EstrategiaDeudaDTO estrategia = debtStrategyService.generarEstrategia(userId, mes, anio);
        List<PagoSugerido> orden = estrategia.getOrdenDePago().stream()
                .map(p -> new PagoSugerido(p.getNombre(), p.getMontoTotal().doubleValue(),
                        p.getTasaInteres().doubleValue(), p.getPagoSugeridoEsteMes().doubleValue()))
                .toList();

        Entrada entrada = new Entrada(
                resumen.getIngresoMensual().doubleValue(),
                resumen.getTotalGastosFijos().doubleValue(),
                resumen.getTotalGastosVariables().doubleValue(),
                resumen.getTotalGastosHormiga().doubleValue(),
                metaAhorro == null ? null : metaAhorro.doubleValue(),
                gastos,
                deudas,
                estrategia.getEstrategiaRecomendada(),
                orden);

        return MotorRecomendaciones.generar(entrada);
    }
}

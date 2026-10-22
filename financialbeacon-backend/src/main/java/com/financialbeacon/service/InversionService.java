package com.financialbeacon.service;

import com.financialbeacon.dto.InversionDTOs.PlanInversionDTO;
import com.financialbeacon.dto.ResumenMensualDTO;
import com.financialbeacon.recomendaciones.MotorInversion;
import com.financialbeacon.recomendaciones.MotorInversion.Entrada;
import com.financialbeacon.recomendaciones.MotorInversion.Perfil;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.DeudaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reúne los datos del mes y las deudas del usuario, y se los entrega al motor
 * de inversión. El perfil, el horizonte y el fondo de emergencia actual vienen
 * del usuario (no se guardan en la base de datos).
 */
@Service
public class InversionService {

    private static final int HORIZONTE_MIN = 1;
    private static final int HORIZONTE_MAX = 480; // 40 años

    @Autowired
    private FinanzasService finanzasService;

    @Autowired
    private DebtStrategyService debtStrategyService;

    public PlanInversionDTO generarPlan(Long userId, Integer mes, Integer anio,
                                        String perfil, Integer horizonteMeses, BigDecimal fondoEmergenciaActual) {
        ResumenMensualDTO resumen = finanzasService.obtenerResumenMensual(userId, mes, anio);

        List<DeudaInfo> deudas = debtStrategyService.listarDeudas(userId).stream()
                .map(d -> new DeudaInfo(d.getNombre(), d.getMontoTotal().doubleValue(),
                        d.getTasaInteres().doubleValue(), d.getCuotaMinima().doubleValue()))
                .toList();

        double esenciales = resumen.getTotalGastosFijos().add(resumen.getTotalGastosVariables()).doubleValue();
        double fondo = fondoEmergenciaActual == null ? 0 : Math.max(fondoEmergenciaActual.doubleValue(), 0);
        int horizonte = horizonteMeses == null ? 60 : Math.min(Math.max(horizonteMeses, HORIZONTE_MIN), HORIZONTE_MAX);

        Entrada entrada = new Entrada(
                resumen.getIngresoMensual().doubleValue(),
                esenciales,
                resumen.getAhorroEstimado().doubleValue(),
                deudas,
                fondo,
                parsearPerfil(perfil),
                horizonte,
                MotorInversion.MESES_FONDO_EMERGENCIA);

        return MotorInversion.generar(entrada);
    }

    private Perfil parsearPerfil(String perfil) {
        if (perfil == null) return Perfil.MODERADO;
        try {
            return Perfil.valueOf(perfil.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Perfil.MODERADO;
        }
    }
}

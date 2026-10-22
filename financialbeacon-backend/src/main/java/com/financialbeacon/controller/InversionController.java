package com.financialbeacon.controller;

import com.financialbeacon.dto.InversionDTOs.PlanInversionDTO;
import com.financialbeacon.service.InversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/inversion")
@CrossOrigin(originPatterns = "*") // dev: acepta localhost y la IP del PC en la red local
public class InversionController {

    @Autowired
    private InversionService inversionService;

    /**
     * perfil: CONSERVADOR | MODERADO | AGRESIVO (por defecto MODERADO)
     * horizonteMeses: en cuántos meses podría necesitar el dinero (por defecto 60)
     * fondoEmergenciaActual: lo que ya tiene ahorrado como fondo de emergencia (por defecto 0)
     */
    @GetMapping
    public ResponseEntity<PlanInversionDTO> obtenerPlan(
            @RequestParam Long userId,
            @RequestParam Integer mes,
            @RequestParam Integer anio,
            @RequestParam(required = false, defaultValue = "MODERADO") String perfil,
            @RequestParam(required = false, defaultValue = "60") Integer horizonteMeses,
            @RequestParam(required = false, defaultValue = "0") BigDecimal fondoEmergenciaActual) {
        return ResponseEntity.ok(
                inversionService.generarPlan(userId, mes, anio, perfil, horizonteMeses, fondoEmergenciaActual));
    }
}

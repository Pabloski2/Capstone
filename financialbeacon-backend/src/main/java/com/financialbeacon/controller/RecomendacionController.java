package com.financialbeacon.controller;

import com.financialbeacon.dto.RecomendacionDTO;
import com.financialbeacon.service.RecomendacionesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/recomendaciones")
@CrossOrigin(originPatterns = "*") // dev: acepta localhost y la IP del PC en la red local
public class RecomendacionController {

    @Autowired
    private RecomendacionesService recomendacionesService;

    @GetMapping
    public ResponseEntity<List<RecomendacionDTO>> obtenerRecomendaciones(
            @RequestParam Long userId,
            @RequestParam Integer mes,
            @RequestParam Integer anio,
            @RequestParam(required = false) BigDecimal metaAhorro) {
        return ResponseEntity.ok(recomendacionesService.generarRecomendaciones(userId, mes, anio, metaAhorro));
    }
}

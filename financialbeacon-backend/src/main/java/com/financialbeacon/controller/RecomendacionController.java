package com.financialbeacon.controller;

import com.financialbeacon.dto.RecomendacionDTO;
import com.financialbeacon.security.AuthInterceptor;
import com.financialbeacon.service.RecomendacionesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/** El id del usuario sale siempre del token (atributo "userId"), nunca de la petición. */
@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionController {

    @Autowired
    private RecomendacionesService recomendacionesService;

    @GetMapping
    public ResponseEntity<List<RecomendacionDTO>> obtenerRecomendaciones(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam Integer mes,
            @RequestParam Integer anio,
            @RequestParam(required = false) BigDecimal metaAhorro) {
        return ResponseEntity.ok(recomendacionesService.generarRecomendaciones(userId, mes, anio, metaAhorro));
    }
}

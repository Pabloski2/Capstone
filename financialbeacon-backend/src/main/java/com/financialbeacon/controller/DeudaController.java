package com.financialbeacon.controller;

import com.financialbeacon.dto.DeudaDTOs.DeudaRequest;
import com.financialbeacon.dto.DeudaDTOs.EstrategiaDeudaDTO;
import com.financialbeacon.model.Deuda;
import com.financialbeacon.security.AuthInterceptor;
import com.financialbeacon.service.DebtStrategyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** El id del usuario sale siempre del token (atributo "userId"), nunca de la petición. */
@RestController
@RequestMapping("/api/deudas")
public class DeudaController {

    @Autowired
    private DebtStrategyService debtStrategyService;

    @PostMapping
    public ResponseEntity<Deuda> registrarDeuda(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody DeudaRequest req) {
        return ResponseEntity.ok(debtStrategyService.registrarDeuda(
                userId, req.getNombre(), req.getMontoTotal(), req.getTasaInteres(), req.getCuotaMinima()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDeuda(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @PathVariable Long id) {
        debtStrategyService.eliminarDeuda(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Deuda>> listarDeudas(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
        return ResponseEntity.ok(debtStrategyService.listarDeudas(userId));
    }

    @GetMapping("/estrategia")
    public ResponseEntity<EstrategiaDeudaDTO> obtenerEstrategia(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        return ResponseEntity.ok(debtStrategyService.generarEstrategia(userId, mes, anio));
    }
}

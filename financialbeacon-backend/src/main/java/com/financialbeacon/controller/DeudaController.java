package com.financialbeacon.controller;

import com.financialbeacon.dto.DeudaDTOs.DeudaRequest;
import com.financialbeacon.dto.DeudaDTOs.EstrategiaDeudaDTO;
import com.financialbeacon.model.Deuda;
import com.financialbeacon.service.DebtStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deudas")
@CrossOrigin(origins = "http://localhost:5173")
public class DeudaController {

    @Autowired
    private DebtStrategyService debtStrategyService;

    @PostMapping
    public ResponseEntity<?> registrarDeuda(@RequestBody DeudaRequest req) {
        try {
            Deuda deuda = debtStrategyService.registrarDeuda(
                    req.getUserId(), req.getNombre(), req.getMontoTotal(),
                    req.getTasaInteres(), req.getCuotaMinima());
            return ResponseEntity.ok(deuda);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Deuda>> listarDeudas(@RequestParam Long userId) {
        return ResponseEntity.ok(debtStrategyService.listarDeudas(userId));
    }

    @GetMapping("/estrategia")
    public ResponseEntity<EstrategiaDeudaDTO> obtenerEstrategia(
            @RequestParam Long userId,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        return ResponseEntity.ok(debtStrategyService.generarEstrategia(userId, mes, anio));
    }
}

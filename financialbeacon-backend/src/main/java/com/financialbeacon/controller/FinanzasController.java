package com.financialbeacon.controller;

import com.financialbeacon.dto.FinanzasRequests.GastoRequest;
import com.financialbeacon.dto.FinanzasRequests.IngresoRequest;
import com.financialbeacon.dto.ResumenMensualDTO;
import com.financialbeacon.model.Gasto;
import com.financialbeacon.model.IngresoMensual;
import com.financialbeacon.service.FinanzasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finanzas")
@CrossOrigin(origins = "http://localhost:5173")
public class FinanzasController {

    @Autowired
    private FinanzasService finanzasService;

    @PostMapping("/ingreso")
    public ResponseEntity<?> registrarIngreso(@RequestBody IngresoRequest req) {
        try {
            IngresoMensual ingreso = finanzasService.registrarIngreso(
                    req.getUserId(), req.getMonto(), req.getMes(), req.getAnio());
            return ResponseEntity.ok(ingreso);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/gasto")
    public ResponseEntity<?> registrarGasto(@RequestBody GastoRequest req) {
        try {
            Gasto gasto = finanzasService.registrarGasto(
                    req.getUserId(), req.getDescripcion(), req.getMonto(),
                    req.getCategoria(), req.getFecha());
            return ResponseEntity.ok(gasto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/gastos")
    public ResponseEntity<List<Gasto>> listarGastos(
            @RequestParam Long userId,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        return ResponseEntity.ok(finanzasService.listarGastosDelMes(userId, mes, anio));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenMensualDTO> obtenerResumen(
            @RequestParam Long userId,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        return ResponseEntity.ok(finanzasService.obtenerResumenMensual(userId, mes, anio));
    }
}

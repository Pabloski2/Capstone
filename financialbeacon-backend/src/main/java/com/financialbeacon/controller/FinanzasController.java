package com.financialbeacon.controller;

import com.financialbeacon.dto.FinanzasRequests.GastoRequest;
import com.financialbeacon.dto.FinanzasRequests.IngresoRequest;
import com.financialbeacon.dto.ResumenMensualDTO;
import com.financialbeacon.model.Gasto;
import com.financialbeacon.model.IngresoMensual;
import com.financialbeacon.security.AuthInterceptor;
import com.financialbeacon.service.FinanzasService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** El id del usuario sale siempre del token (atributo "userId"), nunca de la petición. */
@RestController
@RequestMapping("/api/finanzas")
public class FinanzasController {

    @Autowired
    private FinanzasService finanzasService;

    @PostMapping("/ingreso")
    public ResponseEntity<IngresoMensual> registrarIngreso(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody IngresoRequest req) {
        return ResponseEntity.ok(finanzasService.registrarIngreso(
                userId, req.getMonto(), req.getMes(), req.getAnio()));
    }

    @PostMapping("/gasto")
    public ResponseEntity<Gasto> registrarGasto(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody GastoRequest req) {
        return ResponseEntity.ok(finanzasService.registrarGasto(
                userId, req.getDescripcion(), req.getMonto(), req.getCategoria(), req.getFecha()));
    }

    @DeleteMapping("/gastos/{id}")
    public ResponseEntity<Void> eliminarGasto(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @PathVariable Long id) {
        finanzasService.eliminarGasto(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/gastos")
    public ResponseEntity<List<Gasto>> listarGastos(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        return ResponseEntity.ok(finanzasService.listarGastosDelMes(userId, mes, anio));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenMensualDTO> obtenerResumen(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        return ResponseEntity.ok(finanzasService.obtenerResumenMensual(userId, mes, anio));
    }
}

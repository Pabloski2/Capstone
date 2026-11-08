package com.financialbeacon.controller;

import com.financialbeacon.dto.AuthDTOs.AuthResponse;
import com.financialbeacon.dto.AuthDTOs.EliminarCuentaRequest;
import com.financialbeacon.dto.PerfilDTOs.CambiarEmailRequest;
import com.financialbeacon.dto.PerfilDTOs.CambiarPasswordRequest;
import com.financialbeacon.dto.PerfilDTOs.FotoRequest;
import com.financialbeacon.dto.PerfilDTOs.PerfilDTO;
import com.financialbeacon.dto.PerfilDTOs.PerfilRequest;
import com.financialbeacon.security.AuthInterceptor;
import com.financialbeacon.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Gestión de la cuenta del usuario con sesión iniciada. El id sale siempre del token. */
@RestController
@RequestMapping("/api/cuenta")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<PerfilDTO> obtenerPerfil(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
        return ResponseEntity.ok(cuentaService.obtenerPerfil(userId));
    }

    @PutMapping("/perfil")
    public ResponseEntity<PerfilDTO> guardarPerfil(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody PerfilRequest req) {
        return ResponseEntity.ok(cuentaService.guardarPerfil(userId, req));
    }

    @PostMapping("/email")
    public ResponseEntity<PerfilDTO> cambiarEmail(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody CambiarEmailRequest req) {
        return ResponseEntity.ok(cuentaService.cambiarEmail(userId, req.nuevoEmail(), req.password()));
    }

    @PostMapping("/password")
    public ResponseEntity<AuthResponse> cambiarPassword(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody CambiarPasswordRequest req) {
        return ResponseEntity.ok(cuentaService.cambiarPassword(userId, req.actual(), req.nueva()));
    }

    @PutMapping("/foto")
    public ResponseEntity<PerfilDTO> guardarFoto(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody FotoRequest req) {
        return ResponseEntity.ok(cuentaService.guardarFoto(userId, req.foto()));
    }

    @DeleteMapping("/foto")
    public ResponseEntity<PerfilDTO> eliminarFoto(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
        return ResponseEntity.ok(cuentaService.eliminarFoto(userId));
    }

    @PostMapping("/eliminar")
    public ResponseEntity<Void> eliminar(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody EliminarCuentaRequest req) {
        cuentaService.eliminarCuenta(userId, req.password());
        return ResponseEntity.noContent().build();
    }
}

package com.financialbeacon.controller;

import com.financialbeacon.dto.SoporteDTOs.TicketDTO;
import com.financialbeacon.dto.SoporteDTOs.TicketRequest;
import com.financialbeacon.security.AuthInterceptor;
import com.financialbeacon.service.SoporteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/soporte")
public class SoporteController {

    private final SoporteService soporteService;

    public SoporteController(SoporteService soporteService) {
        this.soporteService = soporteService;
    }

    @PostMapping
    public ResponseEntity<TicketDTO> crear(
            @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody TicketRequest req) {
        return ResponseEntity.ok(soporteService.crear(userId, req));
    }

    @GetMapping
    public ResponseEntity<List<TicketDTO>> listar(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
        return ResponseEntity.ok(soporteService.listar(userId));
    }
}

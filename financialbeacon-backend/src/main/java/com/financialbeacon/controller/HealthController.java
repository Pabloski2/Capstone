package com.financialbeacon.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Ruta pública para que el hosting (Render, Railway, etc.) compruebe que el servidor está vivo. */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("estado", "ok");
    }
}

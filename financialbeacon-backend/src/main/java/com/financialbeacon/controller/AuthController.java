package com.financialbeacon.controller;

import com.financialbeacon.dto.AuthDTOs.AuthResponse;
import com.financialbeacon.dto.AuthDTOs.LoginRequest;
import com.financialbeacon.dto.AuthDTOs.MensajeResponse;
import com.financialbeacon.dto.AuthDTOs.RecuperarRequest;
import com.financialbeacon.dto.AuthDTOs.RegistroRequest;
import com.financialbeacon.dto.AuthDTOs.RestablecerRequest;
import com.financialbeacon.service.AuthService;
import com.financialbeacon.service.RecuperacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RecuperacionService recuperacionService;

    public AuthController(AuthService authService, RecuperacionService recuperacionService) {
        this.authService = authService;
        this.recuperacionService = recuperacionService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /** Responde lo mismo exista o no el correo, para no revelar quién tiene cuenta. */
    @PostMapping("/recuperar")
    public ResponseEntity<MensajeResponse> recuperar(@Valid @RequestBody RecuperarRequest req) {
        recuperacionService.solicitar(req.email());
        return ResponseEntity.ok(new MensajeResponse(
                "Si el correo está registrado, te enviamos un código de 6 dígitos. Revisa también la carpeta de spam."));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<MensajeResponse> restablecer(@Valid @RequestBody RestablecerRequest req) {
        recuperacionService.restablecer(req.email(), req.codigo(), req.nuevaPassword());
        return ResponseEntity.ok(new MensajeResponse("Tu contraseña se actualizó. Ya puedes iniciar sesión."));
    }
}

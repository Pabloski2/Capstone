package com.financialbeacon.service;

import com.financialbeacon.dto.AuthDTOs.AuthResponse;
import com.financialbeacon.dto.AuthDTOs.LoginRequest;
import com.financialbeacon.dto.AuthDTOs.RegistroRequest;
import com.financialbeacon.exception.ApiException;
import com.financialbeacon.model.User;
import com.financialbeacon.repository.UserRepository;
import com.financialbeacon.security.JwtService;
import com.financialbeacon.security.LoginAttemptLimiter;
import com.financialbeacon.util.Correo;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class AuthService {

    private static final int MAX_BYTES_PASSWORD = 72; // límite de BCrypt

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptLimiter limiter;
    // Hash de relleno: se compara aunque el correo no exista, para que el login
    // tarde parecido y no revele qué correos están registrados.
    private final String hashDeRelleno;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, LoginAttemptLimiter limiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.limiter = limiter;
        this.hashDeRelleno = passwordEncoder.encode("relleno-no-es-una-clave-real");
    }

    public AuthResponse register(RegistroRequest req) {
        if (req.password().getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PASSWORD) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña es demasiado larga");
        }
        String email = Correo.normalizar(req.email());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        User user = new User(null, email, passwordEncoder.encode(req.password()), req.nombre().trim());
        return respuesta(userRepository.save(user), jwtService);
    }

    public AuthResponse login(LoginRequest req) {
        String email = Correo.normalizar(req.email());
        if (limiter.estaBloqueado(email)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos fallidos. Espera unos minutos e inténtalo de nuevo.");
        }
        User user = userRepository.findByEmail(email).orElse(null);
        boolean valido = passwordEncoder.matches(req.password(), user != null ? user.getPassword() : hashDeRelleno);
        if (user == null || !valido) {
            limiter.registrarFallo(email);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }
        limiter.registrarExito(email);
        return respuesta(user, jwtService);
    }

    /** Arma la respuesta de sesión (con token nuevo) para un usuario. */
    public static AuthResponse respuesta(User user, JwtService jwtService) {
        return new AuthResponse(user.getId(), user.getEmail(), user.getNombre(),
                jwtService.generar(user.getId()), user.tienePerfilCompleto(), user.getFotoPerfil());
    }
}

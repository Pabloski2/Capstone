package com.financialbeacon.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.financialbeacon.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

/**
 * Exige un token válido (cabecera "Authorization: Bearer ...") en las rutas protegidas
 * y deja el id del usuario en el atributo "userId" de la petición. Los controladores
 * lo leen de ahí: el cliente ya no puede pedir los datos de otro usuario.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "userId";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // Las preflight de CORS no llevan token.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            Optional<JwtService.Sesion> sesion = jwtService.validarSesion(cabecera.substring(7).trim());
            if (sesion.isPresent() && sesionVigente(sesion.get())) {
                request.setAttribute(ATTR_USER_ID, sesion.get().userId());
                return true;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensaje\":\"Tu sesión expiró. Inicia sesión nuevamente.\"}");
        return false;
    }

    /**
     * El token solo vale si el usuario sigue existiendo y no cambió su contraseña después de emitirlo
     * (así, al restablecer la contraseña se cierran las sesiones abiertas en otros dispositivos).
     */
    private boolean sesionVigente(JwtService.Sesion sesion) {
        return userRepository.findCambioPasswordEnById(sesion.userId())
                .map(cambio -> sesion.emitidoEn() >= cambio)
                .orElse(false);
    }
}

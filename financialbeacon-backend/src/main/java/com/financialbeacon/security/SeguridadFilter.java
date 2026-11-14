package com.financialbeacon.security;

import com.financialbeacon.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Filtro único de seguridad. En cada petición:
 *  1. agrega cabeceras de seguridad,
 *  2. aplica CORS (solo los orígenes permitidos pueden usar la API desde un navegador),
 *  3. exige un token de sesión válido en todas las rutas /api/ salvo registro e inicio de sesión,
 *  4. deja el id del usuario autenticado disponible para los controladores.
 */
@Component
public class SeguridadFilter extends OncePerRequestFilter {

    private static final Set<String> RUTAS_PUBLICAS = Set.of("/api/auth/register", "/api/auth/login");
    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OrigenesPermitidos origenes;

    public SeguridadFilter(JwtService jwtService,
                           UserRepository userRepository,
                           @Value("${app.cors.allowed-origins:}") String origenesPermitidos,
                           @Value("${app.cors.allow-private-network:true}") boolean permitirRedLocal) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.origenes = new OrigenesPermitidos(origenesPermitidos, permitirRedLocal);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. Cabeceras de seguridad (las más estrictas solo para la API, que nunca entrega páginas web)
        String ruta = request.getRequestURI();
        response.setHeader("X-Content-Type-Options", "nosniff");
        if (ruta.startsWith("/api/")) {
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("Referrer-Policy", "no-referrer");
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        }

        // 2. CORS
        String origen = request.getHeader("Origin");
        if (origen != null) {
            if (!origenes.permite(origen)) {
                responderError(response, 403, "Origen no permitido");
                return;
            }
            response.setHeader("Access-Control-Allow-Origin", origen);
            response.setHeader("Vary", "Origin");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Max-Age", "3600");
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT); // respuesta a la consulta previa del navegador
            return;
        }

        // 3. Autenticación
        if (ruta.startsWith("/api/") && !RUTAS_PUBLICAS.contains(ruta)) {
            Long userId = autenticar(request);
            if (userId == null) {
                responderError(response, 401, "Tu sesión no es válida o expiró. Inicia sesión de nuevo.");
                return;
            }
            request.setAttribute(SesionActual.ATRIBUTO, userId);
        }
        chain.doFilter(request, response);
    }

    /** Devuelve el id del usuario si el token es válido y la cuenta todavía existe. */
    private Long autenticar(HttpServletRequest request) {
        String cabecera = request.getHeader("Authorization");
        if (cabecera == null || !cabecera.startsWith(PREFIJO_BEARER)) {
            return null;
        }
        return jwtService.validar(cabecera.substring(PREFIJO_BEARER.length()).trim())
                .filter(userRepository::existsById)
                .orElse(null);
    }

    private void responderError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}

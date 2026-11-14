package com.financialbeacon.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita las peticiones por IP y por tipo de ruta (ventana fija, en memoria).
 * Es un interceptor y no un filtro a propósito: así la respuesta 429 también lleva las
 * cabeceras CORS y la app puede leer el mensaje en vez de mostrar "No se pudo conectar".
 * Vive en memoria (se reinicia con el servidor), lo que basta para una sola instancia.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private record Regla(String grupo, int maximo, long ventanaMs) {}

    private static final class Contador {
        long inicioMs;
        int usos;
    }

    private static final Regla LOGIN = new Regla("login", 10, 60_000L);
    private static final Regla REGISTRO = new Regla("registro", 5, 3_600_000L);
    private static final Regla RECUPERAR = new Regla("recuperar", 5, 3_600_000L);
    private static final Regla RESTABLECER = new Regla("restablecer", 10, 3_600_000L);
    private static final Regla GENERAL = new Regla("general", 120, 60_000L);

    private static final int MAX_REGISTROS = 50_000;

    private final ConcurrentHashMap<String, Contador> contadores = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // Las preflight de CORS no cuentan.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String ip = ipCliente(request);
        Regla especifica = switch (request.getRequestURI()) {
            case "/api/auth/login" -> LOGIN;
            case "/api/auth/register" -> REGISTRO;
            case "/api/auth/recuperar" -> RECUPERAR;
            case "/api/auth/restablecer" -> RESTABLECER;
            default -> null;
        };

        boolean excedido = (especifica != null && excedido(ip, especifica)) || excedido(ip, GENERAL);
        if (!excedido) {
            return true;
        }

        response.setStatus(429);
        response.setHeader("Retry-After", "60");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensaje\":\"Demasiadas solicitudes. Espera un momento e inténtalo de nuevo.\"}");
        return false;
    }

    private boolean excedido(String ip, Regla regla) {
        long ahora = System.currentTimeMillis();
        if (contadores.size() > MAX_REGISTROS) { // limpieza para no crecer sin límite
            contadores.entrySet().removeIf(e -> ahora - e.getValue().inicioMs > 3_600_000L);
        }
        boolean[] pasado = new boolean[1];
        contadores.compute(ip + "|" + regla.grupo(), (k, c) -> {
            if (c == null || ahora - c.inicioMs >= regla.ventanaMs()) {
                c = new Contador();
                c.inicioMs = ahora;
            }
            c.usos++;
            if (c.usos > regla.maximo()) {
                pasado[0] = true;
            }
            return c;
        });
        return pasado[0];
    }

    /**
     * Detrás del proxy de Railway la IP real llega en X-Forwarded-For; se toma la última
     * entrada (la que agrega el proxy), porque las anteriores las puede escribir el cliente.
     */
    static String ipCliente(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] partes = xff.split(",");
            return partes[partes.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}

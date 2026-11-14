package com.financialbeacon.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita los intentos fallidos de inicio de sesión: tras 5 fallos en 15 minutos para una misma
 * combinación de dirección y correo, se bloquea por 15 minutos. Frena los ataques de fuerza bruta.
 */
@Component
public class LoginRateLimiter {

    static final int MAX_FALLOS = 5;
    static final Duration VENTANA = Duration.ofMinutes(15);
    private static final int LIMITE_REGISTROS = 10_000;

    private static final class Registro {
        int fallos;
        Instant inicio;
        Instant bloqueadoHasta;
    }

    private final Clock reloj;
    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    public LoginRateLimiter() {
        this(Clock.systemUTC());
    }

    LoginRateLimiter(Clock reloj) {
        this.reloj = reloj;
    }

    public boolean bloqueado(String clave) {
        Registro r = registros.get(clave);
        if (r == null) {
            return false;
        }
        synchronized (r) {
            return r.bloqueadoHasta != null && reloj.instant().isBefore(r.bloqueadoHasta);
        }
    }

    public void registrarFallo(String clave) {
        Instant ahora = reloj.instant();
        if (registros.size() > LIMITE_REGISTROS) {
            limpiarVencidos(ahora);
        }
        Registro r = registros.computeIfAbsent(clave, k -> new Registro());
        synchronized (r) {
            if (r.inicio == null || ahora.isAfter(r.inicio.plus(VENTANA))) {
                r.fallos = 0;
                r.inicio = ahora;
                r.bloqueadoHasta = null;
            }
            r.fallos++;
            if (r.fallos >= MAX_FALLOS) {
                r.bloqueadoHasta = ahora.plus(VENTANA);
            }
        }
    }

    public void registrarExito(String clave) {
        registros.remove(clave);
    }

    private void limpiarVencidos(Instant ahora) {
        registros.entrySet().removeIf(e -> {
            Registro r = e.getValue();
            synchronized (r) {
                return r.inicio == null || ahora.isAfter(r.inicio.plus(VENTANA));
            }
        });
    }
}

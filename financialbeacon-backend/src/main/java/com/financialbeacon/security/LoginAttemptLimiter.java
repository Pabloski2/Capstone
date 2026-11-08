package com.financialbeacon.security;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Frena la adivinación de contraseñas: tras N intentos fallidos seguidos para un mismo correo,
 * se bloquea ese correo por unos minutos. Vive en memoria (se reinicia con el servidor), lo que
 * basta para una sola instancia; con varias instancias convendría guardarlo en base de datos o Redis.
 */
public class LoginAttemptLimiter {

    private static final int MAX_REGISTROS = 10_000;

    private static final class Registro {
        int fallos;
        long bloqueadoHastaMs;
    }

    private final int maxFallos;
    private final long bloqueoMs;
    private final Clock reloj;
    private final ConcurrentHashMap<String, Registro> registros = new ConcurrentHashMap<>();

    public LoginAttemptLimiter(int maxFallos, long bloqueoMs) {
        this(maxFallos, bloqueoMs, Clock.systemUTC());
    }

    LoginAttemptLimiter(int maxFallos, long bloqueoMs, Clock reloj) {
        this.maxFallos = maxFallos;
        this.bloqueoMs = bloqueoMs;
        this.reloj = reloj;
    }

    public boolean estaBloqueado(String clave) {
        Registro r = registros.get(clave);
        return r != null && r.bloqueadoHastaMs > reloj.millis();
    }

    public void registrarFallo(String clave) {
        if (registros.size() > MAX_REGISTROS) {
            long ahora = reloj.millis();
            registros.values().removeIf(r -> r.bloqueadoHastaMs <= ahora && r.fallos < maxFallos);
        }
        registros.compute(clave, (k, r) -> {
            if (r == null) r = new Registro();
            long ahora = reloj.millis();
            if (r.bloqueadoHastaMs != 0 && r.bloqueadoHastaMs <= ahora) { // venció el bloqueo anterior
                r.fallos = 0;
                r.bloqueadoHastaMs = 0;
            }
            r.fallos++;
            if (r.fallos >= maxFallos) {
                r.bloqueadoHastaMs = ahora + bloqueoMs;
            }
            return r;
        });
    }

    public void registrarExito(String clave) {
        registros.remove(clave);
    }
}

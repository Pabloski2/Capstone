package com.financialbeacon.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRateLimiterTest {

    private static RelojManual reloj() {
        return new RelojManual(Instant.parse("2026-11-08T12:00:00Z"));
    }

    @Test
    void bloqueaAlQuintoFallo() {
        LoginRateLimiter limiter = new LoginRateLimiter(reloj());
        for (int i = 0; i < 4; i++) {
            limiter.registrarFallo("1.1.1.1|a@a.cl");
        }
        assertFalse(limiter.bloqueado("1.1.1.1|a@a.cl"));
        limiter.registrarFallo("1.1.1.1|a@a.cl");
        assertTrue(limiter.bloqueado("1.1.1.1|a@a.cl"));
    }

    @Test
    void elBloqueoExpiraDespuesDe15Minutos() {
        RelojManual reloj = reloj();
        LoginRateLimiter limiter = new LoginRateLimiter(reloj);
        for (int i = 0; i < 5; i++) {
            limiter.registrarFallo("k");
        }
        reloj.avanzar(Duration.ofMinutes(14));
        assertTrue(limiter.bloqueado("k"));
        reloj.avanzar(Duration.ofMinutes(2));
        assertFalse(limiter.bloqueado("k"));
    }

    @Test
    void unInicioDeSesionExitosoReiniciaElContador() {
        LoginRateLimiter limiter = new LoginRateLimiter(reloj());
        for (int i = 0; i < 4; i++) {
            limiter.registrarFallo("k");
        }
        limiter.registrarExito("k");
        limiter.registrarFallo("k");
        assertFalse(limiter.bloqueado("k"));
    }

    @Test
    void lasClavesSonIndependientes() {
        LoginRateLimiter limiter = new LoginRateLimiter(reloj());
        for (int i = 0; i < 5; i++) {
            limiter.registrarFallo("atacante");
        }
        assertTrue(limiter.bloqueado("atacante"));
        assertFalse(limiter.bloqueado("otra-persona"));
    }

    @Test
    void losFallosEspaciadosMasDe15MinutosNoSeAcumulan() {
        RelojManual reloj = reloj();
        LoginRateLimiter limiter = new LoginRateLimiter(reloj);
        for (int i = 0; i < 4; i++) {
            limiter.registrarFallo("k");
        }
        reloj.avanzar(Duration.ofMinutes(16));
        limiter.registrarFallo("k");
        assertFalse(limiter.bloqueado("k"));
    }
}

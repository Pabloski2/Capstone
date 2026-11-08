package com.financialbeacon.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptLimiterTest {

    /** Reloj que se puede adelantar a mano. */
    static class RelojMutable extends Clock {
        long ms = Instant.parse("2026-10-22T12:00:00Z").toEpochMilli();
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId z) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(ms); }
        @Override public long millis() { return ms; }
    }

    @Test
    void bloqueaTrasLosFallosPermitidos() {
        RelojMutable reloj = new RelojMutable();
        LoginAttemptLimiter l = new LoginAttemptLimiter(3, 60_000, reloj);
        l.registrarFallo("a@a.cl");
        l.registrarFallo("a@a.cl");
        assertTrue(!l.estaBloqueado("a@a.cl"));
        l.registrarFallo("a@a.cl");
        assertTrue(l.estaBloqueado("a@a.cl"));
    }

    @Test
    void elBloqueoEsPorCorreo() {
        LoginAttemptLimiter l = new LoginAttemptLimiter(1, 60_000, new RelojMutable());
        l.registrarFallo("a@a.cl");
        assertTrue(l.estaBloqueado("a@a.cl"));
        assertTrue(!l.estaBloqueado("b@b.cl"));
    }

    @Test
    void elBloqueoVenceConElTiempo() {
        RelojMutable reloj = new RelojMutable();
        LoginAttemptLimiter l = new LoginAttemptLimiter(1, 60_000, reloj);
        l.registrarFallo("a@a.cl");
        reloj.ms += 59_999;
        assertTrue(l.estaBloqueado("a@a.cl"));
        reloj.ms += 2;
        assertTrue(!l.estaBloqueado("a@a.cl"));
    }

    @Test
    void despuesDeVencerElBloqueo_elContadorEmpiezaDeNuevo() {
        RelojMutable reloj = new RelojMutable();
        LoginAttemptLimiter l = new LoginAttemptLimiter(2, 60_000, reloj);
        l.registrarFallo("a@a.cl");
        l.registrarFallo("a@a.cl");
        reloj.ms += 61_000;
        l.registrarFallo("a@a.cl"); // primer fallo del nuevo ciclo
        assertTrue(!l.estaBloqueado("a@a.cl"));
    }

    @Test
    void unLoginCorrectoReiniciaLosFallos() {
        LoginAttemptLimiter l = new LoginAttemptLimiter(3, 60_000, new RelojMutable());
        l.registrarFallo("a@a.cl");
        l.registrarFallo("a@a.cl");
        l.registrarExito("a@a.cl");
        l.registrarFallo("a@a.cl");
        l.registrarFallo("a@a.cl");
        assertTrue(!l.estaBloqueado("a@a.cl"));
    }
}

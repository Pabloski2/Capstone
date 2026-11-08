package com.financialbeacon.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRETO = "un-secreto-de-prueba-con-mas-de-32-caracteres!";

    private static JwtService servicio(String secreto, long segundos, Instant ahora) {
        return new JwtService(secreto, segundos, Clock.fixed(ahora, ZoneOffset.UTC));
    }

    private static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generarYValidar_devuelveElIdDelUsuario() {
        JwtService jwt = servicio(SECRETO, 3600, Instant.parse("2026-10-22T12:00:00Z"));
        assertEquals(Optional.of(42L), jwt.validar(jwt.generar(42L)));
    }

    @Test
    void validarSesion_devuelveIdYMomentoDeEmision() {
        Instant ahora = Instant.parse("2026-10-22T12:00:00Z");
        JwtService jwt = servicio(SECRETO, 3600, ahora);
        JwtService.Sesion sesion = jwt.validarSesion(jwt.generar(9L)).orElseThrow();
        assertEquals(9L, sesion.userId().longValue());
        assertEquals(ahora.getEpochSecond(), sesion.emitidoEn());
    }

    @Test
    void tokenExpirado_seRechaza() {
        Instant emision = Instant.parse("2026-10-22T12:00:00Z");
        String token = servicio(SECRETO, 3600, emision).generar(7L);
        JwtService mismoSecretoDespues = servicio(SECRETO, 3600, emision.plusSeconds(3601));
        assertTrue(mismoSecretoDespues.validar(token).isEmpty());
    }

    @Test
    void tokenAunVigente_seAcepta() {
        Instant emision = Instant.parse("2026-10-22T12:00:00Z");
        String token = servicio(SECRETO, 3600, emision).generar(7L);
        assertEquals(Optional.of(7L), servicio(SECRETO, 3600, emision.plusSeconds(3599)).validar(token));
    }

    @Test
    void secretoDistinto_seRechaza() {
        Instant ahora = Instant.parse("2026-10-22T12:00:00Z");
        String token = servicio(SECRETO, 3600, ahora).generar(1L);
        assertTrue(servicio("otro-secreto-distinto-de-mas-de-32-caracteres", 3600, ahora).validar(token).isEmpty());
    }

    @Test
    void payloadAlterado_seRechaza() {
        Instant ahora = Instant.parse("2026-10-22T12:00:00Z");
        JwtService jwt = servicio(SECRETO, 3600, ahora);
        String[] partes = jwt.generar(1L).split("\\.");
        long exp = ahora.getEpochSecond() + 3600;
        String falso = b64("{\"sub\":\"2\",\"iat\":" + ahora.getEpochSecond() + ",\"exp\":" + exp + "}");
        assertTrue(jwt.validar(partes[0] + "." + falso + "." + partes[2]).isEmpty());
    }

    @Test
    void tokenSinFirmaOAlgNone_seRechaza() {
        Instant ahora = Instant.parse("2026-10-22T12:00:00Z");
        JwtService jwt = servicio(SECRETO, 3600, ahora);
        String payload = b64("{\"sub\":\"1\",\"iat\":1,\"exp\":" + (ahora.getEpochSecond() + 3600) + "}");
        String sinFirma = b64("{\"alg\":\"none\",\"typ\":\"JWT\"}") + "." + payload + ".";
        assertTrue(jwt.validar(sinFirma).isEmpty());
    }

    @Test
    void basura_seRechazaSinLanzarExcepcion() {
        JwtService jwt = servicio(SECRETO, 3600, Instant.parse("2026-10-22T12:00:00Z"));
        for (String t : new String[]{null, "", "   ", "abc", "a.b", "a.b.c", "a.b.c.d", "....", "%%%.###.@@@"}) {
            assertTrue(jwt.validar(t).isEmpty(), "debía rechazar: " + t);
        }
    }

    @Test
    void secretoCorto_noPermiteCrearElServicio() {
        try {
            new JwtService("corto", 3600);
            throw new AssertionError("debía fallar con un secreto corto");
        } catch (IllegalArgumentException esperado) {
            assertTrue(esperado.getMessage().contains("32"));
        }
    }
}

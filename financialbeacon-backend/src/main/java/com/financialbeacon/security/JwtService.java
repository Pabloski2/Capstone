package com.financialbeacon.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Genera y valida tokens JWT firmados con HMAC-SHA256 (HS256), usando solo la
 * biblioteca estándar de Java. Es una clase simple (sin Spring) para poder
 * probarla de forma aislada.
 *
 * El token lleva el id del usuario (sub), la fecha de emisión (iat) y la de
 * expiración (exp). Solo se aceptan tokens con el encabezado HS256 exacto que
 * genera esta clase, así que un token "alg: none" o con otro algoritmo se rechaza.
 */
public class JwtService {

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_DECODER = Base64.getUrlDecoder();
    private static final String HEADER = B64.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final Pattern SUB = Pattern.compile("\"sub\":\"(\\d{1,18})\"");
    private static final Pattern IAT = Pattern.compile("\"iat\":(\\d{1,12})");
    private static final Pattern EXP = Pattern.compile("\"exp\":(\\d{1,12})");
    private static final int LARGO_MINIMO_SECRETO = 32;

    /** Datos de una sesión válida: quién es y cuándo se emitió el token (segundos desde 1970). */
    public record Sesion(Long userId, long emitidoEn) {}

    private final byte[] secreto;
    private final long duracionSegundos;
    private final Clock reloj;

    public JwtService(String secreto, long duracionSegundos) {
        this(secreto, duracionSegundos, Clock.systemUTC());
    }

    JwtService(String secreto, long duracionSegundos, Clock reloj) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < LARGO_MINIMO_SECRETO) {
            throw new IllegalArgumentException(
                    "El secreto JWT (JWT_SECRET) debe tener al menos " + LARGO_MINIMO_SECRETO + " caracteres");
        }
        if (duracionSegundos <= 0) {
            throw new IllegalArgumentException("La duración del token debe ser positiva");
        }
        this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
        this.duracionSegundos = duracionSegundos;
        this.reloj = reloj;
    }

    public String generar(Long userId) {
        long ahora = reloj.millis() / 1000;
        String payloadJson = "{\"sub\":\"" + userId + "\",\"iat\":" + ahora + ",\"exp\":" + (ahora + duracionSegundos) + "}";
        String payload = B64.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String firmado = HEADER + "." + payload;
        return firmado + "." + B64.encodeToString(firmar(firmado));
    }

    /** Devuelve el id del usuario si el token es auténtico y no ha expirado; si no, vacío. */
    public Optional<Long> validar(String token) {
        return validarSesion(token).map(Sesion::userId);
    }

    /** Igual que validar, pero también entrega cuándo se emitió el token. */
    public Optional<Sesion> validarSesion(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        String[] partes = token.split("\\.", -1);
        if (partes.length != 3 || !HEADER.equals(partes[0])) return Optional.empty();

        try {
            byte[] esperada = firmar(partes[0] + "." + partes[1]);
            byte[] recibida = B64_DECODER.decode(partes[2]);
            if (!MessageDigest.isEqual(esperada, recibida)) return Optional.empty();

            String payload = new String(B64_DECODER.decode(partes[1]), StandardCharsets.UTF_8);
            Matcher exp = EXP.matcher(payload);
            Matcher sub = SUB.matcher(payload);
            Matcher iat = IAT.matcher(payload);
            if (!exp.find() || !sub.find() || !iat.find()) return Optional.empty();
            if (Long.parseLong(exp.group(1)) <= reloj.millis() / 1000) return Optional.empty();
            return Optional.of(new Sesion(Long.parseLong(sub.group(1)), Long.parseLong(iat.group(1))));
        } catch (IllegalArgumentException e) { // Base64 inválido o número fuera de rango
            return Optional.empty();
        }
    }

    private byte[] firmar(String datos) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto, "HmacSHA256"));
            return mac.doFinal(datos.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo firmar el token", e);
        }
    }
}

package com.financialbeacon.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Valida una foto enviada como "data URL" (data:image/jpeg;base64,...). Solo se aceptan JPEG, PNG y WebP,
 * se comprueba que el contenido real coincida con el tipo declarado y se limita el tamaño. SVG queda
 * excluido a propósito (puede llevar scripts).
 */
public final class FotoPerfil {

    public static final int MAX_CARACTERES = 200_000;
    public static final int MAX_BYTES = 150_000;

    private FotoPerfil() {}

    /** Devuelve la foto normalizada o lanza IllegalArgumentException con un mensaje para el usuario. */
    public static String validar(String dataUrl) {
        if (dataUrl == null || dataUrl.length() > MAX_CARACTERES) {
            throw new IllegalArgumentException("La foto es demasiado pesada. Elige una imagen más pequeña.");
        }
        int coma = dataUrl.indexOf(',');
        if (!dataUrl.startsWith("data:image/") || coma < 0 || !dataUrl.substring(0, coma).endsWith(";base64")) {
            throw new IllegalArgumentException("El formato de la foto no es válido.");
        }
        String tipo = dataUrl.substring("data:image/".length(), coma - ";base64".length());

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(dataUrl.substring(coma + 1));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("La foto está dañada. Prueba con otra.");
        }
        if (bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("La foto es demasiado pesada. Elige una imagen más pequeña.");
        }

        boolean coincide = switch (tipo) {
            case "jpeg" -> bytes.length > 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
            case "png" -> bytes.length > 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G';
            case "webp" -> bytes.length > 12
                    && new String(bytes, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
                    && new String(bytes, 8, 4, StandardCharsets.US_ASCII).equals("WEBP");
            default -> false;
        };
        if (!coincide) {
            throw new IllegalArgumentException("Solo se aceptan fotos JPG, PNG o WebP.");
        }
        return dataUrl;
    }
}

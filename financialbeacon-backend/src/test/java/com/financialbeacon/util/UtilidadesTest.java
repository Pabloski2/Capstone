package com.financialbeacon.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilidadesTest {

    private static String dataUrl(String tipo, byte[] bytes) {
        return "data:image/" + tipo + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    private static boolean rechaza(String foto) {
        try {
            FotoPerfil.validar(foto);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    // ---------- FotoPerfil ----------

    @Test
    void foto_jpegValida_seAcepta() {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F'};
        String url = dataUrl("jpeg", jpeg);
        assertEquals(url, FotoPerfil.validar(url));
    }

    @Test
    void foto_pngValida_seAcepta() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13};
        assertEquals(dataUrl("png", png), FotoPerfil.validar(dataUrl("png", png)));
    }

    @Test
    void foto_webpValida_seAcepta() {
        byte[] webp = "RIFF1234WEBPVP8 ".getBytes(StandardCharsets.US_ASCII);
        assertEquals(dataUrl("webp", webp), FotoPerfil.validar(dataUrl("webp", webp)));
    }

    @Test
    void foto_svgSeRechaza() {
        byte[] svg = "<svg xmlns='http://www.w3.org/2000/svg'><script>alert(1)</script></svg>".getBytes(StandardCharsets.UTF_8);
        assertTrue(rechaza(dataUrl("svg+xml", svg)));
    }

    @Test
    void foto_contenidoNoCoincideConElTipo_seRechaza() {
        byte[] texto = "esto no es una imagen".getBytes(StandardCharsets.UTF_8);
        assertTrue(rechaza(dataUrl("jpeg", texto)));
        assertTrue(rechaza(dataUrl("png", texto)));
    }

    @Test
    void foto_formatoInvalido_seRechaza() {
        assertTrue(rechaza(null));
        assertTrue(rechaza(""));
        assertTrue(rechaza("hola"));
        assertTrue(rechaza("data:text/html;base64,PGgxPmhpPC9oMT4="));
        assertTrue(rechaza("data:image/jpeg;base64,%%%no-es-base64%%%"));
        assertTrue(rechaza("data:image/jpeg,sin-base64"));
    }

    @Test
    void foto_demasiadoPesada_seRechaza() {
        byte[] grande = new byte[FotoPerfil.MAX_BYTES + 1];
        grande[0] = (byte) 0xFF; grande[1] = (byte) 0xD8; grande[2] = (byte) 0xFF;
        assertTrue(rechaza(dataUrl("jpeg", grande)));
    }

    // ---------- CodigoRecuperacion ----------

    @Test
    void codigo_siempreTieneSeisDigitos() {
        for (int i = 0; i < 5000; i++) {
            String c = CodigoRecuperacion.generar();
            assertTrue(c.matches("\\d{6}"), "código inválido: " + c);
        }
    }

    @Test
    void codigo_noEsSiempreElMismo() {
        java.util.Set<String> distintos = new java.util.HashSet<>();
        for (int i = 0; i < 200; i++) distintos.add(CodigoRecuperacion.generar());
        assertTrue(distintos.size() > 100);
    }

    // ---------- Edad ----------

    @Test
    void edad_justoLos18_esMayor() {
        LocalDate hoy = LocalDate.of(2026, 10, 22);
        assertTrue(Edad.esMayorDe(LocalDate.of(2008, 10, 22), 18, hoy));
        assertTrue(!Edad.esMayorDe(LocalDate.of(2008, 10, 23), 18, hoy));
    }

    @Test
    void edad_fechaFutura_noEsMayor() {
        assertTrue(!Edad.esMayorDe(LocalDate.of(2030, 1, 1), 18, LocalDate.of(2026, 10, 22)));
    }

    // ---------- Correo ----------

    @Test
    void correo_seNormaliza() {
        assertEquals("ana@gmail.com", Correo.normalizar("  Ana@Gmail.COM "));
    }
}

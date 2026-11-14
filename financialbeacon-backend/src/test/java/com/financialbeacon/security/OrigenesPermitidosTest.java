package com.financialbeacon.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrigenesPermitidosTest {

    @Test
    void permiteDesarrolloLocalYRedPrivada() {
        OrigenesPermitidos o = new OrigenesPermitidos("", true);
        assertTrue(o.permite("http://localhost:5173"));
        assertTrue(o.permite("http://127.0.0.1:5173"));
        assertTrue(o.permite("http://192.168.1.50:5173"));
        assertTrue(o.permite("http://10.0.0.5:5173"));
        assertTrue(o.permite("http://172.16.0.1:5173"));
        assertTrue(o.permite("http://172.31.255.1:5173"));
    }

    @Test
    void rechazaIpPublicasYRangosFueraDeLaRedPrivada() {
        OrigenesPermitidos o = new OrigenesPermitidos("", true);
        assertFalse(o.permite("http://172.32.0.1:5173"));
        assertFalse(o.permite("http://172.15.0.1:5173"));
        assertFalse(o.permite("http://192.169.1.1:5173"));
        assertFalse(o.permite("http://8.8.8.8"));
        assertFalse(o.permite("http://999.1.1.1"));
    }

    @Test
    void rechazaDominiosQueSoloImitanALocalhost() {
        OrigenesPermitidos o = new OrigenesPermitidos("", true);
        assertFalse(o.permite("http://localhost.evil.com"));
        assertFalse(o.permite("http://evil.com"));
        assertFalse(o.permite("http://evil.com/?x=http://localhost"));
        assertFalse(o.permite("http://localhost@evil.com"));
        assertFalse(o.permite("http://192.168.1.5.evil.com"));
    }

    @Test
    void rechazaValoresRaros() {
        OrigenesPermitidos o = new OrigenesPermitidos("", true);
        assertFalse(o.permite(null));
        assertFalse(o.permite(""));
        assertFalse(o.permite("null"));
        assertFalse(o.permite("ftp://localhost"));
        assertFalse(o.permite("http://localhost/ruta"));
        assertFalse(o.permite("no es una url"));
    }

    @Test
    void permiteLosOrigenesConfigurados() {
        OrigenesPermitidos o = new OrigenesPermitidos("https://app.midominio.cl, https://otra.cl/", false);
        assertTrue(o.permite("https://app.midominio.cl"));
        assertTrue(o.permite("https://otra.cl")); // en la configuración se tolera una barra final
        // Un navegador nunca envía el origen con barra final: se exige coincidencia exacta
        assertFalse(o.permite("https://app.midominio.cl/"));
        assertFalse(o.permite("https://intruso.cl"));
    }

    @Test
    void sinRedLocalNoSePermiteLocalhost() {
        OrigenesPermitidos o = new OrigenesPermitidos("https://app.midominio.cl", false);
        assertFalse(o.permite("http://localhost:5173"));
        assertFalse(o.permite("http://192.168.1.50:5173"));
    }
}

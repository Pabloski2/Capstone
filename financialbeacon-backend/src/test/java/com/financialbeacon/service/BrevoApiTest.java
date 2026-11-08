package com.financialbeacon.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueba el cliente contra un servidor HTTP local (no sale a internet ni envía correos reales). */
class BrevoApiTest {

    private static final String CLAVE = "xkeysib-clave-secreta-de-prueba";

    private record Servidor(HttpServer http, AtomicReference<String> metodo, AtomicReference<String> clave,
                            AtomicReference<String> tipo, AtomicReference<String> cuerpo) {}

    private static Servidor levantar(int estado, String respuesta) throws IOException {
        AtomicReference<String> metodo = new AtomicReference<>();
        AtomicReference<String> clave = new AtomicReference<>();
        AtomicReference<String> tipo = new AtomicReference<>();
        AtomicReference<String> cuerpo = new AtomicReference<>();
        HttpServer http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        http.createContext("/v3/smtp/email", ex -> {
            metodo.set(ex.getRequestMethod());
            clave.set(ex.getRequestHeaders().getFirst("api-key"));
            tipo.set(ex.getRequestHeaders().getFirst("Content-Type"));
            cuerpo.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(estado, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.close();
        });
        http.start();
        return new Servidor(http, metodo, clave, tipo, cuerpo);
    }

    private static BrevoApi cliente(Servidor s) {
        String url = "http://127.0.0.1:" + s.http().getAddress().getPort() + "/v3/smtp/email";
        return new BrevoApi(url, CLAVE, "soporte@ejemplo.cl", "Financial Beacon");
    }

    @Test
    void enviaLaPeticionConLosEncabezadosYElCuerpoEsperados() throws Exception {
        Servidor s = levantar(201, "{\"messageId\":\"<abc>\"}");
        try {
            cliente(s).enviar("ana@gmail.com", "Tu código", "Tu código es 123456", "usuario@correo.cl");
        } finally {
            s.http().stop(0);
        }
        assertEquals("POST", s.metodo().get());
        assertEquals(CLAVE, s.clave().get());
        assertTrue(s.tipo().get().startsWith("application/json"));
        String cuerpo = s.cuerpo().get();
        assertTrue(cuerpo.contains("\"sender\":{\"name\":\"Financial Beacon\",\"email\":\"soporte@ejemplo.cl\"}"), cuerpo);
        assertTrue(cuerpo.contains("\"to\":[{\"email\":\"ana@gmail.com\"}]"), cuerpo);
        assertTrue(cuerpo.contains("\"subject\":\"Tu código\""), cuerpo);
        assertTrue(cuerpo.contains("\"textContent\":\"Tu código es 123456\""), cuerpo);
        assertTrue(cuerpo.contains("\"replyTo\":{\"email\":\"usuario@correo.cl\"}"), cuerpo);
    }

    @Test
    void sinResponderA_noIncluyeReplyTo() throws Exception {
        Servidor s = levantar(201, "{}");
        try {
            cliente(s).enviar("ana@gmail.com", "Hola", "Texto", null);
        } finally {
            s.http().stop(0);
        }
        assertTrue(!s.cuerpo().get().contains("replyTo"));
    }

    @Test
    void errorDelProveedor_lanzaExcepcionSinFiltrarLaClave() throws Exception {
        Servidor s = levantar(401, "{\"code\":\"unauthorized\",\"message\":\"Key not found\"}");
        try {
            cliente(s).enviar("ana@gmail.com", "Hola", "Texto", null);
            throw new AssertionError("debía lanzar IOException");
        } catch (IOException esperado) {
            assertTrue(esperado.getMessage().contains("401"), esperado.getMessage());
            assertTrue(!esperado.getMessage().contains(CLAVE), "el mensaje no debe contener la clave");
        } finally {
            s.http().stop(0);
        }
    }

    @Test
    void escapaComillasSaltosDeLineaYCaracteresDeControl() {
        BrevoApi api = new BrevoApi("http://x", CLAVE, "a@b.cl", "Nombre \"raro\"");
        String json = api.construirJson("x@y.cl", "Asunto \"con\" comillas", "Línea 1\nLínea 2\t\\fin\u0001", null);
        assertTrue(json.contains("Nombre \\\"raro\\\""), json);
        assertTrue(json.contains("Asunto \\\"con\\\" comillas"), json);
        assertTrue(json.contains("Línea 1\\nLínea 2\\t\\\\fin\\u0001"), json);
        assertTrue(!json.contains("\n"), "no debe quedar un salto de línea real dentro del JSON");
    }
}

package com.financialbeacon.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Envía correos con la API HTTPS de Brevo (antes Sendinblue), usando solo el cliente HTTP de Java.
 *
 * Existe porque Railway bloquea el SMTP (puertos 25, 465 y 587) en los planes Trial, Free y Hobby,
 * pero permite las llamadas HTTPS. Brevo tiene un plan gratuito de 300 correos diarios.
 * Es una clase simple (sin Spring) para poder probarla contra un servidor local.
 */
public class BrevoApi {

    private final HttpClient http;
    private final String url;
    private final String apiKey;
    private final String remitenteEmail;
    private final String remitenteNombre;

    public BrevoApi(String url, String apiKey, String remitenteEmail, String remitenteNombre) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build(),
                url, apiKey, remitenteEmail, remitenteNombre);
    }

    BrevoApi(HttpClient http, String url, String apiKey, String remitenteEmail, String remitenteNombre) {
        this.http = http;
        this.url = url;
        this.apiKey = apiKey;
        this.remitenteEmail = remitenteEmail;
        this.remitenteNombre = remitenteNombre;
    }

    public void enviar(String para, String asunto, String texto, String responderA)
            throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        construirJson(para, asunto, texto, responderA), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() / 100 != 2) {
            String cuerpo = respuesta.body() == null ? "" : respuesta.body();
            if (cuerpo.length() > 300) cuerpo = cuerpo.substring(0, 300);
            // Nunca se incluye la clave en el mensaje de error.
            throw new IOException("Brevo respondió " + respuesta.statusCode() + ": " + cuerpo);
        }
    }

    String construirJson(String para, String asunto, String texto, String responderA) {
        StringBuilder sb = new StringBuilder(256 + texto.length());
        sb.append("{\"sender\":{\"name\":\"").append(escapar(remitenteNombre))
                .append("\",\"email\":\"").append(escapar(remitenteEmail)).append("\"},")
                .append("\"to\":[{\"email\":\"").append(escapar(para)).append("\"}],")
                .append("\"subject\":\"").append(escapar(asunto)).append("\",")
                .append("\"textContent\":\"").append(escapar(texto)).append("\"");
        if (responderA != null && !responderA.isBlank()) {
            sb.append(",\"replyTo\":{\"email\":\"").append(escapar(responderA)).append("\"}");
        }
        return sb.append('}').toString();
    }

    /** Escapa un texto para ponerlo dentro de un string JSON. */
    static String escapar(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}

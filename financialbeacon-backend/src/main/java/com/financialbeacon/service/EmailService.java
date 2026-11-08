package com.financialbeacon.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Envía correos en segundo plano, para que la respuesta al usuario no dependa de la velocidad del
 * servidor de correo. Hay dos formas de envío, según app.mail.provider (variable MAIL_PROVIDER):
 *
 *  - "brevo": API HTTPS de Brevo. Es la que funciona en Railway (planes Trial, Free y Hobby),
 *             que bloquea el SMTP. Requiere BREVO_API_KEY y MAIL_FROM (remitente verificado en Brevo).
 *  - "smtp":  SMTP clásico (Gmail, etc.). Requiere MAIL_USERNAME y MAIL_PASSWORD. Funciona en
 *             hostings que permiten SMTP (o en Railway con plan Pro).
 *
 * Si no hay credenciales (típico en desarrollo) no falla: escribe el correo en el log del servidor,
 * así se puede probar el flujo completo sin enviar nada. En producción las credenciales son
 * obligatorias: si faltan, el servidor no arranca.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String usuarioSmtp;
    private final String remitente;
    private final boolean usaBrevo;
    private final String brevoApiKey;
    private final BrevoApi brevo;
    private final String perfiles;
    private final ExecutorService ejecutor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "envio-correo");
        t.setDaemon(true);
        return t;
    });

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username:}") String usuarioSmtp,
                        @Value("${app.mail.from:}") String remitente,
                        @Value("${app.mail.provider:smtp}") String proveedor,
                        @Value("${app.mail.brevo.api-key:}") String brevoApiKey,
                        @Value("${app.mail.brevo.url:https://api.brevo.com/v3/smtp/email}") String brevoUrl,
                        @Value("${app.mail.from-name:Financial Beacon}") String nombreRemitente,
                        @Value("${spring.profiles.active:}") String perfiles) {
        this.mailSender = mailSender;
        this.usuarioSmtp = usuarioSmtp;
        this.remitente = remitente == null || remitente.isBlank() ? usuarioSmtp : remitente;
        this.usaBrevo = "brevo".equalsIgnoreCase(proveedor == null ? "" : proveedor.trim());
        this.brevoApiKey = brevoApiKey;
        this.brevo = usaBrevo ? new BrevoApi(brevoUrl, brevoApiKey, this.remitente, nombreRemitente) : null;
        this.perfiles = perfiles == null ? "" : perfiles;
    }

    /** En producción, un correo mal configurado impide arrancar: es mejor enterarse al desplegar que cuando un usuario lo necesita. */
    @PostConstruct
    void verificarConfiguracion() {
        if (perfiles.contains("prod") && !estaConfigurado()) {
            throw new IllegalStateException(usaBrevo
                    ? "Correo sin configurar: faltan BREVO_API_KEY y/o MAIL_FROM (remitente verificado en Brevo)."
                    : "Correo sin configurar: faltan MAIL_USERNAME y/o MAIL_PASSWORD.");
        }
        log.info("Envío de correo: {}{}", usaBrevo ? "API de Brevo" : "SMTP",
                estaConfigurado() ? "" : " (SIN CREDENCIALES: los correos solo se escribirán en el log)");
    }

    public boolean estaConfigurado() {
        if (usaBrevo) {
            return brevoApiKey != null && !brevoApiKey.isBlank() && remitente != null && !remitente.isBlank();
        }
        return usuarioSmtp != null && !usuarioSmtp.isBlank();
    }

    public void enviar(String para, String asunto, String texto) {
        enviar(para, asunto, texto, null);
    }

    /** responderA: si se indica, al pulsar "Responder" el correo va a esa dirección (usado en soporte). */
    public void enviar(String para, String asunto, String texto, String responderA) {
        if (para == null || para.isBlank()) {
            log.warn("No se envió el correo '{}': no hay destinatario configurado", asunto);
            return;
        }
        if (!estaConfigurado()) {
            log.warn("CORREO NO CONFIGURADO (modo desarrollo). Para: {} | Asunto: {}\n{}", para, asunto, texto);
            return;
        }
        ejecutor.submit(() -> {
            try {
                if (usaBrevo) {
                    brevo.enviar(para, asunto, texto, responderA);
                } else {
                    enviarPorSmtp(para, asunto, texto, responderA);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("No se pudo enviar el correo '{}' a {}: {}", asunto, para, e.getMessage());
            }
        });
    }

    private void enviarPorSmtp(String para, String asunto, String texto, String responderA) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(para);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);
        if (responderA != null && !responderA.isBlank()) {
            mensaje.setReplyTo(responderA);
        }
        mailSender.send(mensaje);
    }

    @PreDestroy
    public void cerrar() {
        ejecutor.shutdown();
    }
}

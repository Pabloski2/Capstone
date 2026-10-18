package com.financialbeacon.dto;

/**
 * Una recomendación para el usuario.
 *
 * tipo: AHORRO o DEUDA (indica en qué pantalla se muestra).
 * nivel: ALERTA, AVISO, INFO o POSITIVO (define el color y la urgencia).
 * ahorroPotencial: monto mensual que podría liberar si sigue la recomendación (puede ser null).
 */
public class RecomendacionDTO {

    private final String tipo;
    private final String nivel;
    private final String titulo;
    private final String mensaje;
    private final Double ahorroPotencial;

    public RecomendacionDTO(String tipo, String nivel, String titulo, String mensaje, Double ahorroPotencial) {
        this.tipo = tipo;
        this.nivel = nivel;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.ahorroPotencial = ahorroPotencial;
    }

    public String getTipo() { return tipo; }
    public String getNivel() { return nivel; }
    public String getTitulo() { return titulo; }
    public String getMensaje() { return mensaje; }
    public Double getAhorroPotencial() { return ahorroPotencial; }
}

package com.financialbeacon.util;

import java.security.SecureRandom;

public final class CodigoRecuperacion {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private CodigoRecuperacion() {}

    /** Código numérico de 6 dígitos (con ceros a la izquierda) generado con un generador criptográfico. */
    public static String generar() {
        return String.format("%06d", ALEATORIO.nextInt(1_000_000));
    }
}

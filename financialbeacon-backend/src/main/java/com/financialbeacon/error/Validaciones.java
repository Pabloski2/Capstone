package com.financialbeacon.error;

import java.math.BigDecimal;

/** Validaciones de parámetros de consulta (los cuerpos JSON se validan con anotaciones en los DTO). */
public final class Validaciones {

    private Validaciones() {}

    public static void mesAnio(Integer mes, Integer anio) {
        if (mes == null || mes < 1 || mes > 12) {
            throw new ApiException(400, "El mes debe estar entre 1 y 12");
        }
        if (anio == null || anio < 2000 || anio > 2100) {
            throw new ApiException(400, "El año no es válido");
        }
    }

    /** Si el valor viene informado, debe estar entre el mínimo y el máximo. */
    public static void rangoOpcional(BigDecimal valor, double minimo, double maximo, String nombre) {
        if (valor != null && (valor.doubleValue() < minimo || valor.doubleValue() > maximo)) {
            throw new ApiException(400, "El valor de " + nombre + " no es válido");
        }
    }

    public static void rangoOpcional(Integer valor, int minimo, int maximo, String nombre) {
        if (valor != null && (valor < minimo || valor > maximo)) {
            throw new ApiException(400, "El valor de " + nombre + " no es válido");
        }
    }
}

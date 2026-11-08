package com.financialbeacon.util;

import java.time.LocalDate;
import java.time.Period;

public final class Edad {

    private Edad() {}

    public static boolean esMayorDe(LocalDate nacimiento, int anios, LocalDate hoy) {
        return !nacimiento.isAfter(hoy) && Period.between(nacimiento, hoy).getYears() >= anios;
    }
}

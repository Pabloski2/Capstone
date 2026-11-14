package com.financialbeacon.util;

import com.financialbeacon.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.Set;

/** Reglas mínimas para las contraseñas nuevas (el largo ya lo valida el DTO: 8 a 72 caracteres). */
public final class PoliticaPassword {

    private static final Set<String> COMUNES = Set.of(
            "12345678", "123456789", "1234567890", "password", "password1", "qwerty123",
            "abc12345", "11111111", "contrasena", "contrasena1", "iloveyou1");

    private PoliticaPassword() {}

    public static void validar(String password, String email) {
        boolean letra = password.chars().anyMatch(Character::isLetter);
        boolean numero = password.chars().anyMatch(Character::isDigit);
        if (!letra || !numero) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña debe incluir letras y números");
        }
        String p = password.toLowerCase(Locale.ROOT);
        if (COMUNES.contains(p)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Esa contraseña es demasiado común");
        }
        String local = email == null ? "" : email.split("@")[0].toLowerCase(Locale.ROOT);
        if (local.length() >= 4 && p.contains(local)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña no puede contener tu correo");
        }
    }
}

package com.financialbeacon.util;

import java.util.Locale;

public final class Correo {

    private Correo() {}

    /** Los correos se guardan y se comparan sin espacios y en minúsculas. */
    public static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

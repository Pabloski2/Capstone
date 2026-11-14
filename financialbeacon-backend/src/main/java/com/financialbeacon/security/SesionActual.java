package com.financialbeacon.security;

import com.financialbeacon.error.ApiException;
import jakarta.servlet.http.HttpServletRequest;

/** Entrega el usuario autenticado de la petición actual. Sale del token, nunca de lo que envía el cliente. */
public final class SesionActual {

    public static final String ATRIBUTO = "financialbeacon.userId";

    private SesionActual() {}

    public static Long userId(HttpServletRequest request) {
        Object valor = request.getAttribute(ATRIBUTO);
        if (valor instanceof Long id) {
            return id;
        }
        throw new ApiException(401, "Tu sesión no es válida o expiró. Inicia sesión de nuevo.");
    }
}

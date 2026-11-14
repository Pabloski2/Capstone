package com.financialbeacon.error;

/** Error controlado: lleva el código HTTP y un mensaje seguro para mostrar al usuario. */
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;

    public ApiException(int status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

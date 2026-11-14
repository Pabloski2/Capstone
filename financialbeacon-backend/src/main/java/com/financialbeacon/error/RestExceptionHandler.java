package com.financialbeacon.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.Objects;

/**
 * Convierte cualquier error en una respuesta JSON simple {"mensaje": "..."} con un texto seguro.
 * Nunca se devuelven detalles internos (nombres de tablas, restricciones, trazas).
 */
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    private static ResponseEntity<Map<String, String>> respuesta(int status, String mensaje) {
        return ResponseEntity.status(status).body(Map.of("mensaje", mensaje));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> controlado(ApiException e) {
        return respuesta(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Los datos enviados no son válidos");
        return respuesta(400, mensaje);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, String>> solicitudInvalida(Exception e) {
        return respuesta(400, "La solicitud no es válida. Revisa los datos enviados.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException e) {
        LOG.warn("Violación de integridad de datos: {}", e.getMostSpecificCause().getMessage());
        return respuesta(409, "Los datos no cumplen las reglas de la base de datos.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> inesperado(RuntimeException e) {
        LOG.error("Error inesperado", e);
        return respuesta(500, "Ocurrió un error en el servidor. Inténtalo de nuevo más tarde.");
    }
}

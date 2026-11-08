package com.financialbeacon.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDTOs {

    private AuthDTOs() {}

    public record RegistroRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre es demasiado largo")
            String nombre,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no es válido")
            @Size(max = 255, message = "El correo es demasiado largo")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
            String password) {}

    public record LoginRequest(
            @NotBlank(message = "Ingresa tu correo") String email,
            @NotBlank(message = "Ingresa tu contraseña") String password) {}

    public record RecuperarRequest(
            @NotBlank(message = "Ingresa tu correo")
            @Email(message = "El correo no es válido")
            String email) {}

    public record RestablecerRequest(
            @NotBlank(message = "Ingresa tu correo")
            @Email(message = "El correo no es válido")
            String email,

            @NotBlank(message = "Ingresa el código que te enviamos")
            @Pattern(regexp = "\\d{6}", message = "El código tiene 6 dígitos")
            String codigo,

            @NotBlank(message = "La nueva contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
            String nuevaPassword) {}

    public record EliminarCuentaRequest(
            @NotBlank(message = "Ingresa tu contraseña para confirmar") String password) {}

    public record MensajeResponse(String mensaje) {}

    /** Respuesta de login, registro y cambio de contraseña: datos básicos, token y si falta completar el perfil. */
    public record AuthResponse(Long id, String email, String nombre, String token,
                               boolean perfilCompleto, String fotoPerfil) {}
}

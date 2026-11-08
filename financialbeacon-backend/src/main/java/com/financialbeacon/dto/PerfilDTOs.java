package com.financialbeacon.dto;

import com.financialbeacon.model.TipoIngreso;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class PerfilDTOs {

    private PerfilDTOs() {}

    public record PerfilRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre es demasiado largo")
            String nombre,

            @NotBlank(message = "El apellido es obligatorio")
            @Size(max = 100, message = "El apellido es demasiado largo")
            String apellido,

            @NotNull(message = "La fecha de nacimiento es obligatoria")
            @Past(message = "La fecha de nacimiento no es válida")
            LocalDate fechaNacimiento,

            @NotBlank(message = "El teléfono es obligatorio")
            @Pattern(regexp = "^\\+?[0-9 ()-]{8,20}$", message = "El teléfono no es válido (ejemplo: +56 9 1234 5678)")
            String telefono,

            @NotBlank(message = "La ciudad o comuna es obligatoria")
            @Size(max = 100, message = "La ciudad o comuna es demasiado larga")
            String ciudad,

            @NotBlank(message = "La ocupación es obligatoria")
            @Size(max = 100, message = "La ocupación es demasiado larga")
            String ocupacion,

            @NotNull(message = "Indica cómo recibes tus ingresos")
            TipoIngreso tipoIngreso) {}

    public record PerfilDTO(
            Long id, String email, String nombre, String apellido, LocalDate fechaNacimiento,
            String telefono, String ciudad, String ocupacion, TipoIngreso tipoIngreso,
            boolean perfilCompleto, String fotoPerfil) {}

    public record CambiarEmailRequest(
            @NotBlank(message = "Ingresa el nuevo correo")
            @Email(message = "El correo no es válido")
            @Size(max = 255, message = "El correo es demasiado largo")
            String nuevoEmail,

            @NotBlank(message = "Ingresa tu contraseña para confirmar")
            String password) {}

    public record CambiarPasswordRequest(
            @NotBlank(message = "Ingresa tu contraseña actual") String actual,

            @NotBlank(message = "La nueva contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
            String nueva) {}

    public record FotoRequest(@NotBlank(message = "Falta la foto") String foto) {}
}

package com.financialbeacon.dto;

import com.financialbeacon.model.CategoriaSoporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class SoporteDTOs {

    private SoporteDTOs() {}

    public record TicketRequest(
            @NotBlank(message = "Escribe un asunto")
            @Size(max = 150, message = "El asunto es demasiado largo (máximo 150 caracteres)")
            String asunto,

            @NotNull(message = "Elige una categoría")
            CategoriaSoporte categoria,

            @NotBlank(message = "Cuéntanos qué pasó")
            @Size(min = 10, max = 2000, message = "El mensaje debe tener entre 10 y 2000 caracteres")
            String mensaje) {}

    public record TicketDTO(Long id, String asunto, CategoriaSoporte categoria, String mensaje,
                            String estado, Instant creadoEn) {}
}

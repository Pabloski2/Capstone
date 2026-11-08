package com.financialbeacon.service;

import com.financialbeacon.dto.SoporteDTOs.TicketDTO;
import com.financialbeacon.dto.SoporteDTOs.TicketRequest;
import com.financialbeacon.exception.ApiException;
import com.financialbeacon.model.SoporteTicket;
import com.financialbeacon.model.User;
import com.financialbeacon.repository.SoporteTicketRepository;
import com.financialbeacon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Soporte técnico: guarda cada solicitud, la envía por correo al equipo y confirma al usuario. */
@Service
public class SoporteService {

    static final int MAX_SOLICITUDES_POR_HORA = 5;

    private final SoporteTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final String correoSoporte;

    public SoporteService(SoporteTicketRepository ticketRepository, UserRepository userRepository,
                          EmailService emailService, @Value("${app.soporte.email:}") String correoSoporte) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.correoSoporte = correoSoporte;
    }

    public TicketDTO crear(Long userId, TicketRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Tu sesión no es válida. Inicia sesión nuevamente."));

        Instant ahora = Instant.now();
        if (ticketRepository.countByUserIdAndCreadoEnAfter(userId, ahora.minus(1, ChronoUnit.HOURS)) >= MAX_SOLICITUDES_POR_HORA) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Enviaste varias solicitudes seguidas. Espera un poco antes de enviar otra.");
        }

        SoporteTicket ticket = ticketRepository.save(new SoporteTicket(
                null, req.asunto().trim(), req.categoria(), req.mensaje().trim(), "ABIERTO", ahora, user));

        String nombreCompleto = (user.getNombre() + " " + (user.getApellido() == null ? "" : user.getApellido())).trim();
        // Al equipo de soporte: "Responder" contesta directamente al usuario.
        emailService.enviar(correoSoporte, "[Soporte #" + ticket.getId() + "] " + ticket.getAsunto(),
                "Nueva solicitud de soporte #" + ticket.getId() + "\n"
                        + "Usuario: " + nombreCompleto + " (id " + user.getId() + ") <" + user.getEmail() + ">\n"
                        + "Categoría: " + ticket.getCategoria() + "\n\n"
                        + ticket.getMensaje(),
                user.getEmail());
        // Al usuario: confirmación de recepción.
        emailService.enviar(user.getEmail(), "Recibimos tu solicitud #" + ticket.getId() + " en Financial Beacon",
                "Hola " + user.getNombre() + ",\n\nRecibimos tu solicitud \"" + ticket.getAsunto()
                        + "\". Te responderemos a este correo.\n\nFinancial Beacon");

        return aDto(ticket);
    }

    public List<TicketDTO> listar(Long userId) {
        return ticketRepository.findByUserIdOrderByCreadoEnDesc(userId).stream().map(SoporteService::aDto).toList();
    }

    private static TicketDTO aDto(SoporteTicket t) {
        return new TicketDTO(t.getId(), t.getAsunto(), t.getCategoria(), t.getMensaje(), t.getEstado(), t.getCreadoEn());
    }
}

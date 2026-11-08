package com.financialbeacon.service;

import com.financialbeacon.dto.AuthDTOs.AuthResponse;
import com.financialbeacon.dto.PerfilDTOs.PerfilDTO;
import com.financialbeacon.dto.PerfilDTOs.PerfilRequest;
import com.financialbeacon.exception.ApiException;
import com.financialbeacon.model.User;
import com.financialbeacon.repository.DeudaRepository;
import com.financialbeacon.repository.GastoRepository;
import com.financialbeacon.repository.IngresoMensualRepository;
import com.financialbeacon.repository.RecuperacionPasswordRepository;
import com.financialbeacon.repository.SoporteTicketRepository;
import com.financialbeacon.repository.UserRepository;
import com.financialbeacon.security.JwtService;
import com.financialbeacon.util.Correo;
import com.financialbeacon.util.Edad;
import com.financialbeacon.util.FotoPerfil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;

/** Todo lo que el usuario puede hacer sobre su propia cuenta con la sesión iniciada. */
@Service
public class CuentaService {

    static final int EDAD_MINIMA = 18;
    private static final int MAX_BYTES_PASSWORD = 72;

    private final UserRepository userRepository;
    private final GastoRepository gastoRepository;
    private final DeudaRepository deudaRepository;
    private final IngresoMensualRepository ingresoRepository;
    private final SoporteTicketRepository soporteRepository;
    private final RecuperacionPasswordRepository recuperacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public CuentaService(UserRepository userRepository, GastoRepository gastoRepository,
                         DeudaRepository deudaRepository, IngresoMensualRepository ingresoRepository,
                         SoporteTicketRepository soporteRepository,
                         RecuperacionPasswordRepository recuperacionRepository,
                         PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService) {
        this.userRepository = userRepository;
        this.gastoRepository = gastoRepository;
        this.deudaRepository = deudaRepository;
        this.ingresoRepository = ingresoRepository;
        this.soporteRepository = soporteRepository;
        this.recuperacionRepository = recuperacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public PerfilDTO obtenerPerfil(Long userId) {
        return aDto(buscar(userId));
    }

    /** Sirve para completar el perfil la primera vez y para editarlo después. */
    public PerfilDTO guardarPerfil(Long userId, PerfilRequest req) {
        LocalDate hoy = LocalDate.now();
        if (req.fechaNacimiento().getYear() < 1900) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha de nacimiento no es válida");
        }
        if (!Edad.esMayorDe(req.fechaNacimiento(), EDAD_MINIMA, hoy)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes ser mayor de " + EDAD_MINIMA + " años para usar la aplicación");
        }
        User user = buscar(userId);
        user.setNombre(req.nombre().trim());
        user.setApellido(req.apellido().trim());
        user.setFechaNacimiento(req.fechaNacimiento());
        user.setTelefono(req.telefono().trim());
        user.setCiudad(req.ciudad().trim());
        user.setOcupacion(req.ocupacion().trim());
        user.setTipoIngreso(req.tipoIngreso());
        return aDto(userRepository.save(user));
    }

    public PerfilDTO cambiarEmail(Long userId, String nuevoEmail, String password) {
        User user = buscar(userId);
        verificarPassword(user, password);
        String nuevo = Correo.normalizar(nuevoEmail);
        if (nuevo.equals(user.getEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ese ya es tu correo actual");
        }
        if (userRepository.findByEmail(nuevo).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }
        String anterior = user.getEmail();
        user.setEmail(nuevo);
        User guardado = userRepository.save(user);
        emailService.enviar(anterior, "Cambiaste el correo de tu cuenta de Financial Beacon",
                "Hola " + user.getNombre() + ",\n\nEl correo de tu cuenta ahora es " + nuevo
                        + ". Si no fuiste tú, escríbenos desde la sección de soporte.\n\nFinancial Beacon");
        return aDto(guardado);
    }

    /** Devuelve una sesión nueva: las demás sesiones abiertas dejan de valer. */
    public AuthResponse cambiarPassword(Long userId, String actual, String nueva) {
        User user = buscar(userId);
        verificarPassword(user, actual);
        if (nueva.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PASSWORD) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña es demasiado larga");
        }
        user.setPassword(passwordEncoder.encode(nueva));
        user.setCambioPasswordEn(Instant.now().getEpochSecond());
        User guardado = userRepository.save(user);
        emailService.enviar(guardado.getEmail(), "Cambiaste la contraseña de Financial Beacon",
                "Hola " + guardado.getNombre() + ",\n\nLa contraseña de tu cuenta se cambió hace un momento. "
                        + "Si no fuiste tú, recupera tu cuenta de inmediato.\n\nFinancial Beacon");
        return AuthService.respuesta(guardado, jwtService);
    }

    public PerfilDTO guardarFoto(Long userId, String foto) {
        String valida;
        try {
            valida = FotoPerfil.validar(foto);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        User user = buscar(userId);
        user.setFotoPerfil(valida);
        return aDto(userRepository.save(user));
    }

    public PerfilDTO eliminarFoto(Long userId) {
        User user = buscar(userId);
        user.setFotoPerfil(null);
        return aDto(userRepository.save(user));
    }

    /** Borra la cuenta y todos sus datos. Exige la contraseña. */
    @Transactional
    public void eliminarCuenta(Long userId, String password) {
        User user = buscar(userId);
        verificarPassword(user, password);
        gastoRepository.deleteByUserId(userId);
        deudaRepository.deleteByUserId(userId);
        ingresoRepository.deleteByUserId(userId);
        soporteRepository.deleteByUserId(userId);
        recuperacionRepository.deleteByEmail(user.getEmail());
        userRepository.delete(user);
    }

    // ---- auxiliares ----

    private User buscar(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Tu sesión no es válida. Inicia sesión nuevamente."));
    }

    // 403 y no 401: un 401 haría que la app cierre la sesión en vez de mostrar el error.
    private void verificarPassword(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "La contraseña no es correcta");
        }
    }

    private static PerfilDTO aDto(User u) {
        return new PerfilDTO(u.getId(), u.getEmail(), u.getNombre(), u.getApellido(), u.getFechaNacimiento(),
                u.getTelefono(), u.getCiudad(), u.getOcupacion(), u.getTipoIngreso(),
                u.tienePerfilCompleto(), u.getFotoPerfil());
    }
}

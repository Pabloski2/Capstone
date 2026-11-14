package com.financialbeacon.service;

import com.financialbeacon.exception.ApiException;
import com.financialbeacon.model.RecuperacionPassword;
import com.financialbeacon.model.User;
import com.financialbeacon.repository.RecuperacionPasswordRepository;
import com.financialbeacon.repository.UserRepository;
import com.financialbeacon.util.CodigoRecuperacion;
import com.financialbeacon.util.Correo;
import com.financialbeacon.util.PoliticaPassword;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Recuperación de contraseña por código de 6 dígitos enviado al correo.
 *
 * Medidas: el código vence a los 15 minutos, se guarda con hash, solo se permiten 5 intentos
 * fallidos (después hay que esperar 1 hora), se envía como máximo uno por minuto y la respuesta
 * es igual exista o no el correo (no revela qué correos están registrados).
 */
@Service
public class RecuperacionService {

    static final int VIGENCIA_MINUTOS = 15;
    static final int MAX_INTENTOS = 5;
    static final int ESPERA_ENTRE_ENVIOS_SEGUNDOS = 60;
    static final int BLOQUEO_MINUTOS = 60;
    private static final int MAX_BYTES_PASSWORD = 72;

    private static final String MSG_INVALIDO = "El código no es válido o ya venció. Solicita uno nuevo.";

    private final UserRepository userRepository;
    private final RecuperacionPasswordRepository recuperacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RecuperacionService(UserRepository userRepository, RecuperacionPasswordRepository recuperacionRepository,
                               PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.recuperacionRepository = recuperacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /** Siempre termina sin error: el controlador responde lo mismo exista o no el correo. */
    @Transactional
    public void solicitar(String emailCrudo) {
        String email = Correo.normalizar(emailCrudo);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            passwordEncoder.encode("relleno"); // tarda parecido, para no delatar si el correo existe
            return;
        }

        Instant ahora = Instant.now();
        Optional<RecuperacionPassword> previo = recuperacionRepository.findTopByEmailOrderByCreadoEnDesc(email);
        if (previo.isPresent()) {
            RecuperacionPassword p = previo.get();
            boolean bloqueado = p.getIntentos() >= MAX_INTENTOS
                    && p.getCreadoEn().isAfter(ahora.minus(BLOQUEO_MINUTOS, ChronoUnit.MINUTES));
            boolean muyReciente = p.getCreadoEn().isAfter(ahora.minusSeconds(ESPERA_ENTRE_ENVIOS_SEGUNDOS));
            if (bloqueado || muyReciente) return;
        }

        recuperacionRepository.deleteByEmail(email);
        String codigo = CodigoRecuperacion.generar();
        recuperacionRepository.save(new RecuperacionPassword(
                null, email, passwordEncoder.encode(codigo),
                ahora.plus(VIGENCIA_MINUTOS, ChronoUnit.MINUTES), 0, ahora));

        emailService.enviar(email, "Tu código para restablecer la contraseña de Financial Beacon",
                "Hola " + user.getNombre() + ",\n\n"
                        + "Tu código para restablecer la contraseña es:\n\n"
                        + "    " + codigo + "\n\n"
                        + "Vence en " + VIGENCIA_MINUTOS + " minutos. Si tú no lo pediste, ignora este mensaje: "
                        + "tu contraseña no cambia.\n\n"
                        + "Financial Beacon");
    }

    // noRollbackFor: si el código es incorrecto se lanza una excepción, pero el intento fallido DEBE quedar
    // guardado; sin esto la transacción se revertiría y se podrían probar códigos sin límite.
    @Transactional(noRollbackFor = ApiException.class)
    public void restablecer(String emailCrudo, String codigo, String nuevaPassword) {
        if (nuevaPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PASSWORD) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña es demasiado larga");
        }
        PoliticaPassword.validar(nuevaPassword, emailCrudo);
        String email = Correo.normalizar(emailCrudo);
        Instant ahora = Instant.now();

        RecuperacionPassword registro = recuperacionRepository.findTopByEmailOrderByCreadoEnDesc(email).orElse(null);
        if (registro == null || registro.getExpiraEn().isBefore(ahora)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, MSG_INVALIDO);
        }
        if (registro.getIntentos() >= MAX_INTENTOS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos con este código. Vuelve a intentarlo en una hora.");
        }
        if (!passwordEncoder.matches(codigo, registro.getCodigoHash())) {
            registro.setIntentos(registro.getIntentos() + 1);
            recuperacionRepository.save(registro);
            throw new ApiException(HttpStatus.BAD_REQUEST, MSG_INVALIDO);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, MSG_INVALIDO));
        user.setPassword(passwordEncoder.encode(nuevaPassword));
        user.setCambioPasswordEn(ahora.getEpochSecond()); // cierra las sesiones abiertas
        userRepository.save(user);
        recuperacionRepository.deleteByEmail(email);

        emailService.enviar(email, "Cambiaste la contraseña de Financial Beacon",
                "Hola " + user.getNombre() + ",\n\n"
                        + "La contraseña de tu cuenta se cambió hace un momento. "
                        + "Si no fuiste tú, recupera tu cuenta de inmediato o escríbenos desde la sección de soporte.\n\n"
                        + "Financial Beacon");
    }
}

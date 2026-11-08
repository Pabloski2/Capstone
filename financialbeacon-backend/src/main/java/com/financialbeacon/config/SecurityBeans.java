package com.financialbeacon.config;

import com.financialbeacon.security.JwtService;
import com.financialbeacon.security.LoginAttemptLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBeans {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 5 intentos fallidos seguidos bloquean ese correo por 5 minutos. */
    @Bean
    public LoginAttemptLimiter loginAttemptLimiter() {
        return new LoginAttemptLimiter(5, 5 * 60 * 1000L);
    }

    @Bean
    public JwtService jwtService(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.expiration-hours:168}") long horas) {
        return new JwtService(secreto, horas * 3600);
    }
}

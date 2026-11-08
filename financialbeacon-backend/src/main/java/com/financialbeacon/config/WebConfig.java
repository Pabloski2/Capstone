package com.financialbeacon.config;

import com.financialbeacon.security.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web central: qué rutas exigen sesión y qué orígenes pueden llamar a la API.
 * Los orígenes se definen en app.cors.allowed-origins (incluye los de la app móvil con Capacitor).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final String[] origenesPermitidos;

    public WebConfig(AuthInterceptor authInterceptor,
                     @Value("${app.cors.allowed-origins}") String[] origenesPermitidos) {
        this.authInterceptor = authInterceptor;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}

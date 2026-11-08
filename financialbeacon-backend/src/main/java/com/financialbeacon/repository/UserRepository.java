package com.financialbeacon.repository;

import com.financialbeacon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /**
     * Consulta liviana (no trae la foto) usada en cada petición: devuelve el momento del último cambio
     * de contraseña (0 si nunca cambió) o vacío si el usuario ya no existe.
     */
    @Query("select coalesce(u.cambioPasswordEn, 0L) from User u where u.id = :id")
    Optional<Long> findCambioPasswordEnById(@Param("id") Long id);
}

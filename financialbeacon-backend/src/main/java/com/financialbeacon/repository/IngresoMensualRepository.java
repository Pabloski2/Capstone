package com.financialbeacon.repository;

import com.financialbeacon.model.IngresoMensual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngresoMensualRepository extends JpaRepository<IngresoMensual, Long> {
    Optional<IngresoMensual> findByUserIdAndMesAndAnio(Long userId, Integer mes, Integer anio);
}

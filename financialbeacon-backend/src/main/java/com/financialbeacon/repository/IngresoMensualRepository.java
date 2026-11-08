package com.financialbeacon.repository;

import com.financialbeacon.model.IngresoMensual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngresoMensualRepository extends JpaRepository<IngresoMensual, Long> {

    void deleteByUserId(Long userId);

    Optional<IngresoMensual> findByUserIdAndMesAndAnio(Long userId, Integer mes, Integer anio);
}

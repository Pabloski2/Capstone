package com.financialbeacon.repository;

import com.financialbeacon.model.Categoria;
import com.financialbeacon.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByUserIdAndFechaBetween(Long userId, LocalDate desde, LocalDate hasta);

    List<Gasto> findByUserIdAndFechaBetweenAndCategoria(
            Long userId, LocalDate desde, LocalDate hasta, Categoria categoria);
}

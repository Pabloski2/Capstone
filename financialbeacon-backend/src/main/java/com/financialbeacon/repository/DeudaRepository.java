package com.financialbeacon.repository;

import com.financialbeacon.model.Deuda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeudaRepository extends JpaRepository<Deuda, Long> {

    void deleteByUserId(Long userId);

    List<Deuda> findByUserId(Long userId);

    java.util.Optional<Deuda> findByIdAndUserId(Long id, Long userId);
}

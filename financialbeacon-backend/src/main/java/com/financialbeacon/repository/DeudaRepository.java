package com.financialbeacon.repository;

import com.financialbeacon.model.Deuda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeudaRepository extends JpaRepository<Deuda, Long> {
    List<Deuda> findByUserId(Long userId);
}

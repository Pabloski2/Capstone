package com.financialbeacon.repository;

import com.financialbeacon.model.RecuperacionPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecuperacionPasswordRepository extends JpaRepository<RecuperacionPassword, Long> {

    Optional<RecuperacionPassword> findTopByEmailOrderByCreadoEnDesc(String email);

    void deleteByEmail(String email);
}

package com.financialbeacon.repository;

import com.financialbeacon.model.SoporteTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SoporteTicketRepository extends JpaRepository<SoporteTicket, Long> {

    List<SoporteTicket> findByUserIdOrderByCreadoEnDesc(Long userId);

    long countByUserIdAndCreadoEnAfter(Long userId, Instant desde);

    void deleteByUserId(Long userId);
}

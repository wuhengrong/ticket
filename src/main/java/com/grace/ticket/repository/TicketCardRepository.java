package com.grace.ticket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grace.ticket.entity.TicketCard;

@Repository
public interface TicketCardRepository extends JpaRepository<TicketCard, Long> {
    List<TicketCard> findAllByOrderBySerialNumberAsc();
    Optional<TicketCard> findBySerialNumber(Integer serialNumber);
    Optional<TicketCard> findByGeneratedCode(String generatedCode);
}
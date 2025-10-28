package com.grace.ticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grace.ticket.entity.TicketInfo;

//TicketInfoRepository.java
@Repository
public interface TicketInfoRepository extends JpaRepository<TicketInfo, Long> {
 List<TicketInfo> findByTicketNumber(String ticketNumber);
 List<TicketInfo> findByBoardingStation(String boardingStation);
}
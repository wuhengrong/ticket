package com.grace.ticket.repository;

import com.grace.ticket.entity.VipCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VipCardRepository extends JpaRepository<VipCard, Long> {
    
    Optional<VipCard> findByCardNumber(String cardNumber);
    
    List<VipCard> findByStatus(VipCard.CardStatus status);
    
    //按照expiryTime先失效的排在前面
    @Query("SELECT vc FROM VipCard vc WHERE vc.status = 'AVAILABLE' AND (vc.expiryTime IS NULL OR vc.expiryTime > :currentTime) ORDER BY vc.expiryTime ASC NULLS LAST")
    List<VipCard> findAvailableCards(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT vc FROM VipCard vc WHERE vc.status = 'IN_USE' AND vc.boardingStation = :boardingStation")
    List<VipCard> findInUseCardsByStation(@Param("boardingStation") String boardingStation);
}
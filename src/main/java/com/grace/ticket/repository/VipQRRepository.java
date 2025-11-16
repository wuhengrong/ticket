package com.grace.ticket.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.grace.ticket.entity.VipQR;

@Repository
public interface VipQRRepository extends JpaRepository<VipQR, Long> {
    
    Optional<VipQR> findByCardUrl(String cardUrl);
    
    List<VipQR> findByStatus(VipQR.QRStatus status);
    
    List<VipQR> findByCreator(String creator);
    
    List<VipQR> findByUserName(String userName);
    
    @Query("SELECT q FROM VipQR q WHERE q.cardUrl LIKE %:keyword% OR q.creator LIKE %:keyword% OR q.userName LIKE %:keyword%")
    List<VipQR> searchByKeyword(@Param("keyword") String keyword);
    
    long countByStatus(VipQR.QRStatus status);
}
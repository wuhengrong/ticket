package com.grace.ticket.repository;

import com.grace.ticket.entity.VipRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VipRecordRepository extends JpaRepository<VipRecord, Long> {
    
    List<VipRecord> findByVipCustomerIdOrderByBoardingTimeDesc(Long vipCustomerId);
    
 // 在VipRecordRepository中添加
    @Query("SELECT vr FROM VipRecord vr WHERE vr.vipCardId = :cardId AND vr.alightingTime IS NULL")
    List<VipRecord> findActiveRecordsByCardId(@Param("cardId") Long cardId);
    
    List<VipRecord> findByVipCardIdOrderByBoardingTimeDesc(Long vipCardId);
    
    // 方法1：使用@Query注解（推荐）
    @Query("SELECT vr FROM VipRecord vr WHERE vr.vipCardId = :cardId AND vr.vipCustomerId = :customerId AND vr.alightingTime IS NULL ORDER BY vr.boardingTime DESC")
    List<VipRecord> findActiveRecordsByCardAndCustomer(@Param("cardId") Long cardId, @Param("customerId") Long customerId);
    
    // 新增：查找客户进行中的使用记录（未出站的记录）
    List<VipRecord> findByVipCustomerIdAndAlightingTimeIsNull(Long vipCustomerId);
}
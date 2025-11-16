package com.grace.ticket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.grace.ticket.entity.VipQrRecord;

@Repository
public interface VipQrRecordRepository extends JpaRepository<VipQrRecord, Long> {
    
    @Query("SELECT q FROM VipQrRecord q WHERE q.customerId = :customerId AND q.status = 'ACTIVE'")
    VipQrRecord findActiveByCustomerId(@Param("customerId") Long customerId);
    
    
    // 根据客户ID查找活跃的二维码记录
    Optional<VipQrRecord> findByCustomerIdAndStatus(Long customerId, String status);
    
    // 根据状态查找二维码记录
    List<VipQrRecord> findByStatus(String status);
    
    // 搜索二维码记录
    @Query("SELECT q FROM VipQrRecord q WHERE " +
    	       "q.qrUrl LIKE %:keyword% OR " +
    	       "q.startStation LIKE %:keyword% OR " +
    	       "q.endStation LIKE %:keyword% OR " +
    	       "q.userName LIKE %:keyword% OR " +  // 新增
    	       "q.nickName LIKE %:keyword%")       // 新增
    	List<VipQrRecord> searchByKeyword(String keyword);
    
    // 统计各状态数量
    @Query("SELECT q.status, COUNT(q) FROM VipQrRecord q GROUP BY q.status")
    List<Object[]> countByStatus();
}
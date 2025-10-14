package com.grace.ticket.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.grace.ticket.entity.VirtualCard;

//VirtualCardRepository.java
@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, String> {
 
 @Query("SELECT v FROM VirtualCard v WHERE v.id = (SELECT g.virtualCardId FROM Group g WHERE g.groupId = :groupId)")
 Optional<VirtualCard> findByGroupId(@Param("groupId") String groupId);
 
 @Query("SELECT v FROM VirtualCard v WHERE v.currentUsedBy = :userId")
 Optional<VirtualCard> findByCurrentUser(@Param("userId") String userId);
 
 /**
  * 查找需要重置的虚拟卡
  * cardInitialStartTime < (当前时间 - 1天)
  */
 @Query("SELECT vc FROM VirtualCard vc WHERE vc.cardInitialStartTime < :thresholdTime")
 List<VirtualCard> findByCardInitialStartTimeBefore(@Param("thresholdTime") LocalDateTime thresholdTime);
 
 /**
  * 查找所有 cardInitialStartTime 不为空的卡
  */
 List<VirtualCard> findByCardInitialStartTimeIsNotNull();
 
 /**
  * 查找特定状态的卡（可选）
  */
 List<VirtualCard> findByPeriodStatus(Integer periodStatus);
}
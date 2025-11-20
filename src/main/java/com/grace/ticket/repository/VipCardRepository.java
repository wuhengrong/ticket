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
    
    
    /**
     * 查找包含指定用户名的预定卡片
     * 支持逗号分隔的多个用户名
     */
    @Query("SELECT vc FROM VipCard vc WHERE vc.reservedUser LIKE %:userName%")
    List<VipCard> findByReservedUserContaining(@Param("userName") String userName);
    
    /**
     * 更精确的查询：检查用户名是否在逗号分隔的列表中
     */
    @Query("SELECT vc FROM VipCard vc WHERE " +
           "vc.reservedUser IS NOT NULL AND " +
           "(vc.reservedUser = :userName OR " +
           "vc.reservedUser LIKE CONCAT(:userName, ',%') OR " +
           "vc.reservedUser LIKE CONCAT('%,', :userName, ',%') OR " +
           "vc.reservedUser LIKE CONCAT('%,', :userName))")
    List<VipCard> findByReservedUserInList(@Param("userName") String userName);
    
    /**
     * 查找状态为RESERVED且包含指定用户名的预定卡片
     */
    @Query("SELECT vc FROM VipCard vc WHERE " +
           "vc.status = 'RESERVED' AND " +
           "vc.reservedUser IS NOT NULL AND " +
           "(vc.reservedUser = :userName OR " +
           "vc.reservedUser LIKE CONCAT(:userName, ',%') OR " +
           "vc.reservedUser LIKE CONCAT('%,', :userName, ',%') OR " +
           "vc.reservedUser LIKE CONCAT('%,', :userName))")
    List<VipCard> findReservedCardsByUserName(@Param("userName") String userName);
    
    
    // 新增：查找已使用过的次卡（expiryTime不为空）
    @Query("SELECT vc FROM VipCard vc WHERE vc.status = 'AVAILABLE' AND vc.expiryTime IS NOT NULL AND (vc.expiryTime IS NULL OR vc.expiryTime > :currentTime) ORDER BY vc.expiryTime ASC NULLS LAST")
    List<VipCard> findUsedCards(@Param("currentTime") LocalDateTime currentTime);
    
    // 新增：查找新的次卡（expiryTime为空）
    @Query("SELECT vc FROM VipCard vc WHERE vc.status = 'AVAILABLE' AND vc.expiryTime IS NULL ORDER BY vc.createdTime ASC")
    List<VipCard> findNewCards();
    
    // 新增：查找备用卡（RESERVED状态）
    @Query("SELECT vc FROM VipCard vc WHERE vc.status = 'RESERVED' ORDER BY vc.expiryTime ASC NULLS LAST")
    List<VipCard> findReservedCards();
    
    // 新增：查找备用卡（STANDBY状态）
    @Query("SELECT vc FROM VipCard vc WHERE vc.status = 'STANDBY' ORDER BY vc.expiryTime ASC NULLS LAST")
    List<VipCard> findStandbyCards();
    
    
}
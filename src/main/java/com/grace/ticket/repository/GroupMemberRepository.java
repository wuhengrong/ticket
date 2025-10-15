package com.grace.ticket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.grace.ticket.entity.GroupMember;

import jakarta.transaction.Transactional;

//GroupMemberRepository.java
@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
 
 Optional<GroupMember> findByGroupIdAndUserId(String groupId, String userId);
 
 List<GroupMember> findByGroupId(String groupId);
 
 List<GroupMember> findByGroupIdAndStatus(String groupId, String status);
 
//在 GroupMemberRepository 中添加
Optional<GroupMember> findByAccessCode(String accessCode);
 
 @Query("SELECT gm FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.useOrder = :useOrder")
 Optional<GroupMember> findByGroupIdAndUseOrder(@Param("groupId") String groupId, 
                                               @Param("useOrder") Integer useOrder);
 
 
 
 @Modifying
 @Transactional
 @Query("DELETE FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.userId = :userId")
 void deleteByGroupIdAndUserId(@Param("groupId") String groupId, @Param("userId") String userId);
}
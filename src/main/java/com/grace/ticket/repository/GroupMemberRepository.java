package com.grace.ticket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.grace.ticket.entity.GroupMember;

//GroupMemberRepository.java
@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
 
 Optional<GroupMember> findByGroupIdAndUserId(String groupId, String userId);
 
 List<GroupMember> findByGroupId(String groupId);
 
 List<GroupMember> findByGroupIdAndStatus(String groupId, String status);
 
 @Query("SELECT gm FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.useOrder = :useOrder")
 Optional<GroupMember> findByGroupIdAndUseOrder(@Param("groupId") String groupId, 
                                               @Param("useOrder") Integer useOrder);
}
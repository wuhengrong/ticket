package com.grace.ticket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.grace.ticket.entity.UsageRecord;

//UsageRecordRepository.java
@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, String> {
 
 List<UsageRecord> findByGroupIdOrderByStartTimeDesc(String groupId);
 
 List<UsageRecord> findByGroupIdAndUserIdOrderByStartTimeDesc(String groupId, String userId);
 
 @Query("SELECT ur FROM UsageRecord ur WHERE ur.groupId = :groupId AND ur.status = 'IN_PROGRESS'")
 Optional<UsageRecord> findCurrentUsage(@Param("groupId") String groupId);
 
 @Query("SELECT ur FROM UsageRecord ur WHERE ur.groupId = :groupId AND ur.userId = :userId AND ur.status = 'IN_PROGRESS'")
 Optional<UsageRecord> findCurrentUsageByUser(@Param("groupId") String groupId, 
                                             @Param("userId") String userId);
 
 @Query("SELECT ur FROM UsageRecord ur WHERE ur.groupId = :groupId AND ur.userId = :userId AND ur.status = 'COMPLETED' ORDER BY ur.endTime DESC")
 List<UsageRecord> findLastCompletedUsage(@Param("groupId") String groupId, 
                                         @Param("userId") String userId);
 
 @Query("SELECT ur FROM UsageRecord ur WHERE ur.groupId = :groupId ORDER BY ur.startTime DESC LIMIT :limit")
 List<UsageRecord> findRecentRecords(@Param("groupId") String groupId, 
                                    @Param("limit") int limit);
}
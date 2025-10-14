package com.grace.ticket.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grace.ticket.dto.UsageResponse;
import com.grace.ticket.entity.Group;
import com.grace.ticket.entity.GroupMember;
import com.grace.ticket.entity.UsageRecord;
import com.grace.ticket.entity.VirtualCard;
import com.grace.ticket.repository.GroupMemberRepository;
import com.grace.ticket.repository.GroupRepository;
import com.grace.ticket.repository.UsageRecordRepository;
import com.grace.ticket.repository.VirtualCardRepository;

import jakarta.transaction.Transactional;

//GroupUsageService.java
@Service
@Transactional
public class GroupUsageService {
 
 @Autowired
 private VirtualCardRepository virtualCardRepository;
 
 @Autowired
 private GroupRepository groupRepository;
 
 @Autowired
 private GroupMemberRepository groupMemberRepository;
 
 @Autowired
 private UsageRecordRepository usageRecordRepository;
 
 private static final String RECORD_PREFIX = "UR";
 
 public UsageResponse startUsage(String groupId, String userId) {
	    // 验证分组是否存在且活跃
	    Group group = groupRepository.findByGroupId(groupId)
	            .orElseThrow(() -> new RuntimeException("分组不存在"));
	    
	    if (!group.isActive()) {
	        return UsageResponse.failed("分组未激活");
	    }
	    
	    // 验证用户是否在分组中且活跃
	    GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
	            .orElseThrow(() -> new RuntimeException("用户不在该分组中"));
	    
	    if (!member.isActive()) {
	        return UsageResponse.failed("用户状态不可用");
	    }
	    
	    // 验证虚拟卡状态
	    VirtualCard virtualCard = virtualCardRepository.findByGroupId(groupId)
	            .orElseThrow(() -> new RuntimeException("虚拟乘车卡不存在"));
	    
	    if (!virtualCard.canUserUseCard(groupId, userId, member.getUseOrder())) {
	        return UsageResponse.failed("当前无法使用票卡");
	    }
	    
	    // 检查是否已有进行中的使用记录
	    Optional<UsageRecord> currentUsage = usageRecordRepository.findCurrentUsage(groupId);
	    if (currentUsage.isPresent()) {
	        return UsageResponse.failed("票卡正在被其他用户使用");
	    }
	    
	    // 开始使用 - 更新虚拟卡状态
	    virtualCard.setCurrentUsedBy(userId);
	    virtualCard.setCurrentUsageStartTime(LocalDateTime.now());
	    
	    //记录第一个人使用时间，也就是虚拟卡初始化时间
	    if(member.getUseOrder() ==1)
	    virtualCard.setCardInitialStartTime(LocalDateTime.now());
	    
	   
	    
	    // 创建使用记录
	    UsageRecord record = new UsageRecord();
	    record.setRecordId(generateRecordId());
	    record.setGroupId(groupId);
	    record.setUserId(userId);
	    record.setVirtualCardId(virtualCard.getId());
	    record.setStartTime(LocalDateTime.now());
	    record.setStatus("IN_PROGRESS");
	    record.setCreateTime(LocalDateTime.now());
	    usageRecordRepository.save(record);
	    
	    return UsageResponse.success("开始使用成功", record);
	}
 
 public UsageResponse endUsage(String groupId, String userId) {
     // 验证分组和用户
     Group group = groupRepository.findByGroupId(groupId)
             .orElseThrow(() -> new RuntimeException("分组不存在"));
     
     GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
             .orElseThrow(() -> new RuntimeException("用户不在该分组中"));
     
     // 获取虚拟卡
     VirtualCard virtualCard = virtualCardRepository.findByGroupId(groupId)
             .orElseThrow(() -> new RuntimeException("虚拟乘车卡不存在"));
     
     // 检查当前用户是否正在使用
     if (!userId.equals(virtualCard.getCurrentUsedBy())) {
         return UsageResponse.failed("您当前没有使用票卡");
     }
     
     // 获取当前使用记录
     UsageRecord currentRecord = usageRecordRepository.findCurrentUsageByUser(groupId, userId)
             .orElseThrow(() -> new RuntimeException("未找到进行中的使用记录"));
     
     // 结束使用
     LocalDateTime endTime = LocalDateTime.now();
     LocalDateTime startTime = currentRecord.getStartTime();
     long duration = Duration.between(startTime, endTime).toMinutes();
     
     // 更新使用记录
     currentRecord.setEndTime(endTime);
     currentRecord.setDuration((int) duration);
     currentRecord.setStatus("COMPLETED");
     usageRecordRepository.save(currentRecord);
     
     // 更新虚拟卡状态
     virtualCard.setCurrentUsedBy(null);
     virtualCard.setCurrentUsageStartTime(null);
     virtualCard.setCurrentStatus(virtualCard.getCurrentStatus() + 1); // 当前状态+1
     virtualCard.setPeriodStatus(virtualCard.getPeriodStatus() + 1); // 当前状态+1
     virtualCardRepository.save(virtualCard);
     
     return UsageResponse.success("归还票卡成功", duration);
 }
 
 public List<UsageRecord> getUsageHistory(String groupId, int limit) {
     return usageRecordRepository.findRecentRecords(groupId, limit);
 }
 
 public GroupMember getUserInfo(String groupId, String userId) {
     return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
             .orElseThrow(() -> new RuntimeException("用户信息不存在"));
 }
 
 private String generateRecordId() {
     return RECORD_PREFIX + System.currentTimeMillis();
 }
}
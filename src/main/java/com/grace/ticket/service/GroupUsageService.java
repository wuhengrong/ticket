package com.grace.ticket.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        
        // 记录第一个人使用时间，也就是虚拟卡初始化时间
        if (member.getUseOrder() == 1) {
            virtualCard.setCardInitialStartTime(LocalDateTime.now());
        }
        
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
        
        // 获取当前组的所有成员
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(groupId);
        
        // 获取下一个用户的使用序号
        int nextUserOrder = virtualCard.getCurrentStatus() + 1;
        
        // 检查下一个用户是否休假 user1= 1 4 user2=2 3
        //boolean nextUserOnLeave = isNextUserOnLeave(groupMembers, nextUserOrder);
        //获取下一个可用序号
        while(TicketUtilService.isNextUserOnLeave(groupMembers, nextUserOrder)) {
        	nextUserOrder ++;
        }
        
        virtualCard.setCurrentStatus(nextUserOrder);
        virtualCard.setPeriodStatus(nextUserOrder);
        
        // 更新虚拟卡状态
        virtualCard.setCurrentUsedBy(null);
        virtualCard.setCurrentUsageStartTime(null);
        virtualCardRepository.save(virtualCard); 
        
        return UsageResponse.success("归还票卡成功", duration);
    }
    
    /**
     * 检查下一个用户是否休假
     */
    private boolean isNextUserOnLeave(List<GroupMember> groupMembers, int nextUserOrder) {
        for (GroupMember member : groupMembers) {
            // 解析用户的使用顺序
            List<Integer> userOrders = parseUseOrder(member.getUseOrder());
            
            // 检查用户是否包含下一个使用序号且处于休假状态
            if (userOrders.contains(nextUserOrder) && "ON_LEAVE".equals(member.getStatus())) {
                System.out.println("用户 " + member.getUserName() + " (使用顺序: " + member.getUseOrder() + 
                                 ") 在序号 " + nextUserOrder + " 处于休假状态");
                return true;
            }
        }
        
        System.out.println("序号 " + nextUserOrder + " 的用户处于正常状态");
        return false;
    }
    
    /**
     * 计算需要跳过的用户数量
     * 处理连续休假的情况
     */
    private int calculateSkipCount(List<GroupMember> groupMembers, int startOrder) {
        int skipCount = 1;
        int currentOrder = startOrder;
        
        while (true) {
            boolean foundOnLeave = false;
            
            for (GroupMember member : groupMembers) {
                List<Integer> userOrders = parseUseOrder(member.getUseOrder());
                
                // 检查下一个顺序是否由休假用户占用
                if (userOrders.contains(currentOrder + skipCount) && "ON_LEAVE".equals(member.getStatus())) {
                    foundOnLeave = true;
                    skipCount++;
                    break;
                }
            }
            
            if (!foundOnLeave) {
                break;
            }
            
            // 防止无限循环
            if (skipCount > groupMembers.size() * 2) {
                System.err.println("跳过数量超过限制，可能存在循环依赖");
                break;
            }
        }
        
        return skipCount;
    }
    
    /**
     * 解析使用顺序（兼容两种格式）
     * 格式1: 13 -> [1, 3] (每个数字代表一个使用顺序)
     * 格式2: 24 -> [2, 4]
     */
    private List<Integer> parseUseOrder(Integer useOrder) {
        List<Integer> result = new ArrayList<>();
        
        if (useOrder == null) {
            return result;
        }
        
        String useOrderStr = String.valueOf(useOrder);
        
        try {
            // 将数字字符串分解为单个数字
            for (char c : useOrderStr.toCharArray()) {
                result.add(Character.getNumericValue(c));
            }
        } catch (NumberFormatException e) {
            System.err.println("解析使用顺序失败: " + useOrderStr);
        }
        
        return result;
    }
    
    /**
     * 分析两种测试场景
     */
    public void analyzeTestScenarios() {
        System.out.println("=== 休假逻辑分析 ===");
        
        // 场景1: user1=13, user2=24
        System.out.println("场景1: user1=13, user2=24");
        analyzeScenario(13, 24);
        
        // 场景2: user1=14, user2=23  
        System.out.println("\n场景2: user1=14, user2=23");
        analyzeScenario(14, 23);
    }
    
    private void analyzeScenario(int user1Order, int user2Order) {
        List<Integer> user1Orders = parseUseOrder(user1Order);
        List<Integer> user2Orders = parseUseOrder(user2Order);
        
        System.out.println("user1 使用顺序: " + user1Orders);
        System.out.println("user2 使用顺序: " + user2Orders);
        
        // 分析每个使用顺序
        for (int i = 1; i <= 4; i++) {
            boolean user1HasOrder = user1Orders.contains(i);
            boolean user2HasOrder = user2Orders.contains(i);
            
            System.out.println("顺序 " + i + ": user1=" + user1HasOrder + ", user2=" + user2HasOrder);
            
            if (user1HasOrder && user2HasOrder) {
                System.out.println("  ⚠️  冲突: 两个用户都有顺序 " + i);
            }
        }
        
        // 测试user2休假的情况
        System.out.println("当user2休假时:");
        for (int currentStatus = 0; currentStatus < 4; currentStatus++) {
            int nextOrder = currentStatus + 1;
            boolean user2HasNextOrder = user2Orders.contains(nextOrder);
            
            if (user2HasNextOrder) {
                System.out.println("  当前状态=" + currentStatus + ", 下一个顺序=" + nextOrder + 
                                 " → user2休假，需要跳过");
                // 找到下一个可用的顺序
                int skipTo = findNextAvailableOrder(user1Orders, user2Orders, nextOrder, true);
                System.out.println("    跳过到顺序: " + skipTo);
            } else {
                System.out.println("  当前状态=" + currentStatus + ", 下一个顺序=" + nextOrder + 
                                 " → 正常使用");
            }
        }
    }
    
    /**
     * 找到下一个可用的使用顺序
     */
    private int findNextAvailableOrder(List<Integer> user1Orders, List<Integer> user2Orders, 
                                     int startOrder, boolean user2OnLeave) {
        int nextOrder = startOrder + 1;
        
        while (true) {
            boolean user2HasOrder = user2Orders.contains(nextOrder);
            boolean user1HasOrder = user1Orders.contains(nextOrder);
            
            // 如果user2休假且占有该顺序，继续跳过
            if (user2OnLeave && user2HasOrder) {
                nextOrder++;
            } 
            // 如果user1占有该顺序，可以使用
            else if (user1HasOrder) {
                return nextOrder;
            }
            // 如果都没有占有该顺序，说明顺序配置有问题
            else {
                System.out.println("  顺序 " + nextOrder + " 没有被任何用户占用");
                return nextOrder;
            }
            
            // 防止无限循环
            if (nextOrder > 10) {
                return startOrder + 1; // 回退到默认
            }
        }
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
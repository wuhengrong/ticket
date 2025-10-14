package com.grace.ticket.config;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.grace.ticket.entity.Group;
import com.grace.ticket.entity.GroupMember;
import com.grace.ticket.entity.VirtualCard;
import com.grace.ticket.repository.GroupMemberRepository;
import com.grace.ticket.repository.GroupRepository;
import com.grace.ticket.repository.VirtualCardRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private VirtualCardRepository virtualCardRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 初始化虚拟卡数据
        initializeVirtualCards();
        
        // 初始化分组数据
        initializeGroups();
        
        // 初始化组成员数据
        initializeGroupMembers();
        
        System.out.println("测试数据初始化完成 - 共生成" + virtualCardRepository.count() + "张虚拟卡，" 
                         + groupRepository.count() + "个分组，" 
                         + groupMemberRepository.count() + "个组成员");
    }
    
    private void initializeVirtualCards() {
        List<VirtualCard> virtualCards = Arrays.asList(
            // 虚拟卡1 - 工作日使用
            createVirtualCard("VC001", "2025.10.11;2025.10.12", 1, 1, 
                "15687556072", "Aa123456", "1318920379", "Aa123456", "1;3;5;7", 0),
            
            
            // 虚拟卡2 - 全天可用
            createVirtualCard("VC002", "2025.09.25;2025.09.26", 1, 1, 
                "17820635357", "5Test12345", "13692585850", "Test5850", "1;3;5;7", 0),
            
            // 虚拟卡3 - 周末使用
            createVirtualCard("VC003", "2025.10.01;2025.10.02", 1, 1, 
                "13800138003", "pass789", "13800138004", "pass101", "2;4;6", 0)
            
            
            
        );
        
        virtualCardRepository.saveAll(virtualCards);
        System.out.println("虚拟卡数据初始化完成");
    }
    
    private void initializeGroups() {
        List<Group> groups = Arrays.asList(
            // 分组1 - 技术部
            createGroup("GRP001", "技术部共享组", 60, "VC001", "ACTIVE"),
            
            // 分组2 - 市场部
            createGroup("GRP002", "市场部分享组", 60, "VC002", "ACTIVE"),
            
            // 分组3 - 研发团队（未激活）
            createGroup("GRP003", "研发团队组", 60, "VC003", "INACTIVE")
            
           
        );
        
        groupRepository.saveAll(groups);
        System.out.println("分组数据初始化完成");
    }
    
    private void initializeGroupMembers() {
        List<GroupMember> groupMembers = Arrays.asList(
            // 分组1成员
            createGroupMember("GRP001", "USER001", 13, 60, "06:00", "23:00", "ACTIVE"),
            createGroupMember("GRP001", "USER002", 24, 60, "06:00", "23:00", "ACTIVE"),
            createGroupMember("GRP001", "USER003", 5, 60, "06:00", "23:00", "ACTIVE"),
            createGroupMember("GRP001", "USER004", 6, 60, "06:00", "23:00", "ACTIVE"),
            
            // 分组2成员
            createGroupMember("GRP002", "USER005", 1, null, "06:00", "23:00", "ACTIVE"),
            createGroupMember("GRP002", "USER006", 2, 40, "06:00", "23:00", "ACTIVE"),//ON_LEAVE
            createGroupMember("GRP002", "USER007", 3, 45, "06:00", "23:00", "ACTIVE")
            
           
        );
        
        // 设置个人URL
        for (GroupMember member : groupMembers) {
            String personalUrl = String.format("http://localhost:8080/mobile/pages/count-card.html?userId=%s&groupId=%s", 
                member.getUserId(), member.getGroupId());
            member.setPersonalUrl(personalUrl);
        }
        
        groupMemberRepository.saveAll(groupMembers);
        System.out.println("组成员数据初始化完成");
    }
    
    private VirtualCard createVirtualCard(String id, String notAvailableDates, Integer status, 
                                         Integer periodStatus, String phoneA, String pwdA, 
                                         String phoneB, String pwdB, String usageRule, Integer currentStatus) {
        return createVirtualCard(id, notAvailableDates, status, periodStatus, phoneA, pwdA, phoneB, pwdB, usageRule, currentStatus, null);
    }
    
    private VirtualCard createVirtualCard(String id, String notAvailableDates, Integer status, 
                                         Integer periodStatus, String phoneA, String pwdA, 
                                         String phoneB, String pwdB, String usageRule, 
                                         Integer currentStatus, String currentUsedBy) {
        VirtualCard card = new VirtualCard();
        card.setId(id);
        card.setNotAvailableDates(notAvailableDates);
        card.setStatus(status);
        card.setPeriodStatus(periodStatus);
        card.setPhysicalCardAPhone(phoneA);
        card.setPhysicalCardAPwd(pwdA);
        card.setPhysicalCardBPhone(phoneB);
        card.setPhysicalCardBPwd(pwdB);
        card.setUsageRule(usageRule);
        card.setCurrentStatus(currentStatus);
        card.setCurrentUsedBy(currentUsedBy);
        if (currentUsedBy != null) {
            card.setCurrentUsageStartTime(LocalDateTime.now().minusMinutes(10)); // 假设已经开始使用10分钟
        }
        return card;
    }
    
    private Group createGroup(String groupId, String groupName, Integer defaultInterval, 
                             String virtualCardId, String status) {
        Group group = new Group();
        group.setGroupId(groupId);
        group.setGroupName(groupName);
        group.setDefaultInterval(defaultInterval);
        group.setVirtualCardId(virtualCardId);
        group.setCreateTime(LocalDateTime.now());
        group.setStatus(status);
        return group;
    }
    
    private GroupMember createGroupMember(String groupId, String userId, Integer useOrder, 
                                        Integer customInterval, String startTime, String endTime, 
                                        String status) {
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setUseOrder(useOrder);
        member.setCustomInterval(customInterval);
        member.setUserCardStartTime(startTime);
        member.setUserCardEndTime(endTime);
        member.setStatus(status);
        return member;
    }
}
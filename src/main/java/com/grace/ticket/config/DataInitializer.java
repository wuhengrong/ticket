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
                "13800138001", "pass123", "13800138002", "pass456", "1;3;5;7", 0),
            
            // 虚拟卡2 - 周末使用
            createVirtualCard("VC002", "2025.10.01;2025.10.02", 1, 1, 
                "13800138003", "pass789", "13800138004", "pass101", "2;4;6", 0),
            
            // 虚拟卡3 - 全天可用
            createVirtualCard("VC003", "2025.12.25;2026.01.01", 1, 1, 
                "13800138005", "pass102", "13800138006", "pass103", "1;2;3;4;5;6;7", 0),
            
            // 虚拟卡4 - 不可用状态
            createVirtualCard("VC004", "2025.10.15;2025.10.16", 0, 0, 
                "13800138007", "pass104", "13800138008", "pass105", "1;3;5", 0),
            
            // 虚拟卡5 - 周二周四使用
            createVirtualCard("VC005", "2025.11.11;2025.11.12", 1, 1, 
                "13800138009", "pass106", "13800138010", "pass107", "2;4", 0),
            
            // 虚拟卡6 - 周一周五使用
            createVirtualCard("VC006", "2025.10.20", 1, 1, 
                "13800138011", "pass108", "13800138012", "pass109", "1;5", 0),
            
            // 虚拟卡7 - 当前有用户在使用
            createVirtualCard("VC007", "2025.10.25;2025.10.26", 1, 1, 
                "13800138013", "pass110", "13800138014", "pass111", "1;3;5", 1, "USER003"),
            
            // 虚拟卡8 - 状态为2（下一个用户使用）
            createVirtualCard("VC008", "2025.11.05", 1, 1, 
                "13800138015", "pass112", "13800138016", "pass113", "1;3;5", 2),
            
            // 虚拟卡9 - 状态为3
            createVirtualCard("VC009", "2025.12.24;2025.12.25", 1, 1, 
                "13800138017", "pass114", "13800138018", "pass115", "2;4;6", 3),
            
            // 虚拟卡10 - 节假日不可用
            createVirtualCard("VC010", "2025.10.30;2025.10.31", 1, 1, 
                "13800138019", "pass116", "13800138020", "pass117", "1;2;3;4;5", 0)
        );
        
        virtualCardRepository.saveAll(virtualCards);
        System.out.println("虚拟卡数据初始化完成");
    }
    
    private void initializeGroups() {
        List<Group> groups = Arrays.asList(
            // 分组1 - 技术部
            createGroup("GRP001", "技术部共享组", 30, "VC001", "ACTIVE"),
            
            // 分组2 - 市场部
            createGroup("GRP002", "市场部分享组", 45, "VC002", "ACTIVE"),
            
            // 分组3 - 研发团队（未激活）
            createGroup("GRP003", "研发团队组", 25, "VC003", "INACTIVE"),
            
            // 分组4 - 测试小组
            createGroup("GRP004", "测试小组", 35, "VC004", "ACTIVE"),
            
            // 分组5 - 产品部
            createGroup("GRP005", "产品部分享", 40, "VC005", "ACTIVE"),
            
            // 分组6 - 运营团队
            createGroup("GRP006", "运营团队", 50, "VC006", "ACTIVE"),
            
            // 分组7 - 设计小组（未激活）
            createGroup("GRP007", "设计小组", 30, "VC007", "INACTIVE"),
            
            // 分组8 - 客服团队
            createGroup("GRP008", "客服团队", 35, "VC008", "ACTIVE"),
            
            // 分组9 - 管理组
            createGroup("GRP009", "管理组", 20, "VC009", "ACTIVE"),
            
            // 分组10 - 临时工作组
            createGroup("GRP010", "临时工作组", 60, "VC010", "ACTIVE")
        );
        
        groupRepository.saveAll(groups);
        System.out.println("分组数据初始化完成");
    }
    
    private void initializeGroupMembers() {
        List<GroupMember> groupMembers = Arrays.asList(
            // 分组1成员
            createGroupMember("GRP001", "USER001", 1, 25, "09:00", "17:00", "ACTIVE"),
            createGroupMember("GRP001", "USER002", 2, 30, "09:00", "17:00", "ACTIVE"),
            createGroupMember("GRP001", "USER003", 3, 35, "09:00", "17:00", "ACTIVE"),
            createGroupMember("GRP001", "USER004", 4, null, "09:00", "17:00", "ACTIVE"),
            
            // 分组2成员
            createGroupMember("GRP002", "USER005", 1, null, "09:00", "18:00", "ACTIVE"),
            createGroupMember("GRP002", "USER006", 2, 40, "09:00", "18:00", "ON_LEAVE"),
            createGroupMember("GRP002", "USER007", 3, 45, "09:00", "18:00", "ACTIVE"),
            
            // 分组3成员
            createGroupMember("GRP003", "USER008", 1, 20, "08:00", "17:00", "INACTIVE"),
            createGroupMember("GRP003", "USER009", 2, null, "08:00", "17:00", "INACTIVE"),
            
            // 分组4成员
            createGroupMember("GRP004", "USER010", 1, null, "10:00", "16:00", "ACTIVE"),
            createGroupMember("GRP004", "USER011", 2, 30, "10:00", "16:00", "ACTIVE"),
            
            // 分组5成员
            createGroupMember("GRP005", "USER012", 1, 25, "08:30", "17:30", "ACTIVE"),
            createGroupMember("GRP005", "USER013", 2, null, "08:30", "17:30", "ACTIVE"),
            createGroupMember("GRP005", "USER014", 3, 35, "08:30", "17:30", "ACTIVE"),
            
            // 分组6成员
            createGroupMember("GRP006", "USER015", 1, 50, "09:00", "18:00", "ACTIVE"),
            createGroupMember("GRP006", "USER016", 2, 55, "09:00", "18:00", "ACTIVE"),
            
            // 分组7成员
            createGroupMember("GRP007", "USER017", 1, 30, "09:00", "17:30", "INACTIVE"),
            
            // 分组8成员
            createGroupMember("GRP008", "USER018", 1, null, "08:00", "20:00", "ACTIVE"),
            createGroupMember("GRP008", "USER019", 2, 35, "08:00", "20:00", "ACTIVE"),
            createGroupMember("GRP008", "USER020", 3, 40, "08:00", "20:00", "ACTIVE"),
            
            // 分组9成员
            createGroupMember("GRP009", "USER021", 1, 20, "08:00", "17:00", "ACTIVE"),
            createGroupMember("GRP009", "USER022", 2, null, "08:00", "17:00", "ACTIVE"),
            
            // 分组10成员
            createGroupMember("GRP010", "USER023", 1, 60, "00:00", "23:59", "ACTIVE"),
            createGroupMember("GRP010", "USER024", 2, 65, "00:00", "23:59", "ACTIVE"),
            createGroupMember("GRP010", "USER025", 3, 70, "00:00", "23:59", "ON_LEAVE")
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
package com.grace.ticket.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.grace.ticket.entity.Group;
import com.grace.ticket.entity.GroupMember;
import com.grace.ticket.entity.TicketCard;
import com.grace.ticket.entity.TicketInfo;
import com.grace.ticket.entity.VipCard;
import com.grace.ticket.entity.VipCustomer;
import com.grace.ticket.entity.VirtualCard;
import com.grace.ticket.repository.GroupMemberRepository;
import com.grace.ticket.repository.GroupRepository;
import com.grace.ticket.repository.TicketInfoRepository;
import com.grace.ticket.repository.VipCardRepository;
import com.grace.ticket.repository.VipCustomerRepository;
import com.grace.ticket.repository.VirtualCardRepository;
import com.grace.ticket.service.SecureUrlService;
import com.grace.ticket.service.TicketCardService;

@Component
public class DataInitializer implements CommandLineRunner {
	//private static final String DOMAIN = "https://ticket-1-6sz7.onrender.com";
    //private static final String DOMAIN = "http://localhost:8080";
    
    @Autowired
    private VirtualCardRepository virtualCardRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    
    @Autowired
    private SecureUrlService secureUrlService;
    
    @Autowired
    private TicketCardService ticketCardService;
    
    @Autowired
    private final TicketInfoRepository ticketInfoRepository;
    
    @Autowired
    private VipCardRepository vipCardRepository;
    
    @Autowired
    private VipCustomerRepository vipCustomerRepository;

    public DataInitializer(TicketInfoRepository ticketInfoRepository) {
        this.ticketInfoRepository = ticketInfoRepository;
    }

    
    public VirtualCardRepository getVirtualCardRepository() {
		return virtualCardRepository;
	}

	public VipCardRepository getVipCardRepository() {
		return vipCardRepository;
	}


	public void setVipCardRepository(VipCardRepository vipCardRepository) {
		this.vipCardRepository = vipCardRepository;
	}


	public VipCustomerRepository getVipCustomerRepository() {
		return vipCustomerRepository;
	}


	public void setVipCustomerRepository(VipCustomerRepository vipCustomerRepository) {
		this.vipCustomerRepository = vipCustomerRepository;
	}


	public void setVirtualCardRepository(VirtualCardRepository virtualCardRepository) {
		this.virtualCardRepository = virtualCardRepository;
	}

	public GroupRepository getGroupRepository() {
		return groupRepository;
	}

	public void setGroupRepository(GroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	public GroupMemberRepository getGroupMemberRepository() {
		return groupMemberRepository;
	}

	public void setGroupMemberRepository(GroupMemberRepository groupMemberRepository) {
		this.groupMemberRepository = groupMemberRepository;
	}

	public SecureUrlService getSecureUrlService() {
		return secureUrlService;
	}

	public void setSecureUrlService(SecureUrlService secureUrlService) {
		this.secureUrlService = secureUrlService;
	}

	public TicketCardService getTicketCardService() {
		return ticketCardService;
	}

	public void setTicketCardService(TicketCardService ticketCardService) {
		this.ticketCardService = ticketCardService;
	}

	public TicketInfoRepository getTicketInfoRepository() {
		return ticketInfoRepository;
	}


	// 获取北京时间
    private LocalDateTime getBeijingTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    }
    
    private LocalDate getBeijingDate() {
        return LocalDate.now(ZoneId.of("Asia/Shanghai"));
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 初始化虚拟卡数据
        initializeVirtualCards();
        
        // 初始化分组数据
        initializeGroups();
        
        // 初始化组成员数据
        initializeGroupMembers();
        
        // 初始化票卡url数据
        initSampleUrlData();
        
        // 初始化票卡最后行程数据
        initializeTicketInfoData();
        
        initializeVipCards();
        initializeVipCustomers();
        
        System.out.println("测试数据初始化完成 - 共生成" + virtualCardRepository.count() + "张虚拟卡，" 
                         + groupRepository.count() + "个分组，" 
                         + groupMemberRepository.count() + "个组成员");
    }
    
    
    /**
     * 初始化VIP卡数据
     */
    private void initializeVipCards() {
        if (vipCardRepository.count() == 0) {
            List<VipCard> vipCards = Arrays.asList(
            		  createVipCard("19002009147", "Aa112233", VipCard.CardStatus.IN_USE, 
            				  LocalDateTime.parse("2025-11-08 16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), //初次使用时间
            				  LocalDateTime.parse("2025-11-08 16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), //有效时间
                              LocalDateTime.parse("2025-11-08 16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), //进站时间
                              "元芬", 
                              LocalDateTime.parse("2025-11-08 16:35", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),//出站时间
                              "东门", 
                              VipCard.InOutStatus.IN, null),
            		  
            		  createVipCard("17701999040", "Aa112233", VipCard.CardStatus.AVAILABLE, 
            				  LocalDateTime.parse("2025-11-08 07:50", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), //初次使用时间
            				  LocalDateTime.parse("2025-11-09 07:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), //有效时间
                              LocalDateTime.parse("2025-11-08 12:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), //进站时间
                              "湾厦", 
                              LocalDateTime.parse("2025-11-08 15:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),//出站时间
                              "岗厦北", 
                              VipCard.InOutStatus.OUT, null),
            		  
                createVipCard("18027147763", "Aa112233", VipCard.CardStatus.AVAILABLE, 
                		 LocalDateTime.parse("2025-11-08 12:50", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), //初次使用时间
       				  	 LocalDateTime.parse("2025-11-09 12:50", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), //有效时间
                         LocalDateTime.parse("2025-11-08 12:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), //进站时间
                         "湾厦", 
                         LocalDateTime.parse("2025-11-08 15:34", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),//出站时间
                         "下水径", 
                         VipCard.InOutStatus.OUT, null),
                
                createVipCard("18027240680", "Aa112233", VipCard.CardStatus.UNAVAILABLE, 
                    LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusDays(1),
                    null, null, null, null, null, null),
                
                createVipCard("19002007047", "Aa112233", VipCard.CardStatus.STANDBY, 
                    null, LocalDateTime.now().plusMonths(6), null, null, null, null, null, null)
                
              
            );

            vipCardRepository.saveAll(vipCards);
            System.out.println("初始化VIP卡数据完成，共创建 " + vipCards.size() + " 张VIP卡");
        } else {
            System.out.println("VIP卡数据已存在，跳过初始化");
        }
    }

    /**
     * 创建VIP卡对象
     */
    private VipCard createVipCard(String cardNumber, String cardPassword, VipCard.CardStatus status,
                                 LocalDateTime firstUseTime, LocalDateTime expiryTime,
                                 LocalDateTime boardingTime, String boardingStation,
                                 LocalDateTime alightingTime, String alightingStation,
                                 VipCard.InOutStatus inOutStatus, LocalDateTime estimatedAlightingTime) {
        VipCard card = new VipCard();
        card.setCardNumber(cardNumber);
        card.setCardPassword(cardPassword);
        card.setStatus(status);
        card.setFirstUseTime(firstUseTime);
        card.setExpiryTime(expiryTime);
        card.setBoardingTime(boardingTime);
        card.setBoardingStation(boardingStation);
        card.setAlightingTime(alightingTime);
        card.setAlightingStation(alightingStation);
        card.setInOutStatus(inOutStatus);
        card.setEstimatedAlightingTime(estimatedAlightingTime);
        return card;
    }

    /**
     * 初始化VIP客户数据
     */
    private void initializeVipCustomers() {
        if (vipCustomerRepository.count() == 0) {
            List<VipCustomer> vipCustomers = Arrays.asList(
                createVipCustomer("张三", 1001L, "技术部", 15, "技术部高级会员"),
                createVipCustomer("李四", 1001L, "技术部", 8, "技术部普通会员"),
                createVipCustomer("王五", 1002L, "销售部", 20, "销售部VIP"),
                createVipCustomer("赵六", 1003L, "市场部", 5, "市场部新会员"),
                createVipCustomer("钱七", 1002L, "销售部", 12, "销售部高级会员")
            );

            vipCustomerRepository.saveAll(vipCustomers);
            System.out.println("初始化VIP客户数据完成，共创建 " + vipCustomers.size() + " 个VIP客户");
        } else {
            System.out.println("VIP客户数据已存在，跳过初始化");
        }
    }

    /**
     * 创建VIP客户对象
     */
    private VipCustomer createVipCustomer(String userName, Long groupId, String groupName, 
                                        Integer rideCount, String remark) {
        VipCustomer customer = new VipCustomer();
        customer.setUserName(userName);
        customer.setGroupId(groupId);
        customer.setGroupName(groupName);
        customer.setRideCount(rideCount);
        customer.setRemark(remark);
        
        // 生成访问码和VIP URL
        String accessCode = secureUrlService.generateSimpleFixedAccessCode(userName, groupId.toString());
        String vipUrl = String.format("vip.html?uId=%s&gId=%s&code=%s", userName, groupId, accessCode);
        customer.setVipUrl(vipUrl);
        
        return customer;
    }
    
    private void initializeTicketInfoData() {
        // 检查是否已有数据，避免重复初始化
        if (ticketInfoRepository.count() == 0) {
            // 第一行数据
            TicketInfo ticket1 = new TicketInfo();
            ticket1.setTicketNumber("17820635357");
            ticket1.setAlightingStation("科学馆");
            ticket1.setAlightingTime(LocalDateTime.of(2025, 10, 28, 16, 50, 0));

            // 第二行数据
            TicketInfo ticket2 = new TicketInfo();
            ticket2.setTicketNumber("17820635358");
            ticket2.setAlightingTime(LocalDateTime.of(2025, 10, 28, 16, 50, 0));
            ticket2.setAlightingStation("沙田");
            

            // 保存数据
            ticketInfoRepository.save(ticket1);
            ticketInfoRepository.save(ticket2);

            System.out.println("TicketInfo表数据初始化完成，共插入2条记录");
        } else {
            System.out.println("TicketInfo表已有数据，跳过初始化");
        }
    }
    
    private void initializeVirtualCards() {
        // 获取当前北京时间
        LocalDate currentDate = getBeijingDate();
        LocalDateTime currentDateTime = getBeijingTime();
        
        List<VirtualCard> virtualCards = Arrays.asList(
            // 虚拟卡1 - 工作日使用
            createVirtualCard("VC001", "自有卡1", "2025.10.11;2025.10.12", 1, 1, 
                "13189203798", "Aa123456", "19372315927", "Aa123456", "1;3;5;7", 1, null,
                // 新增字段
                currentDate.plusDays(0),                    // 使用日期：明天
                null,                                // 当天账号：手机A
                null,                                   // 当天密码：密码A
                null,                                 // 昨天账号：手机B
                null,                                    // 昨天密码：密码B
                null,                                       // 当前使用者
                null                                        // 当前使用时间
            ),
            // 虚拟卡2 - 正在使用的卡
            createVirtualCard("VC002", "借用卡1", "2025.10.01;2025.10.02", 1, 1, 
                "17796260629", "Aa668899", "18202006469", "Aa668899", "1;2;3;4;5;6;7", 1, "USER001",
                // 新增字段
                currentDate.plusDays(0),                    // 使用日期：明天
                null,                                // 当天账号：手机A
                null,                                   // 当天密码：密码A
                null,                                 // 昨天账号：手机B
                null,                                    // 昨天密码：密码B
                null,                                       // 当前使用者
                null                                        // 当前使用时间
            ),
            // 虚拟卡3 - 全天可用
            createVirtualCard("VC003", "自有卡2", "2025.09.25;2025.09.26", 1, 1, 
            		"15922411329", "Aaaa1111", "13358302048", "Aa123456", "1;3;5;7", 1, null,
                // 新增字段
                currentDate.plusDays(0),                    // 使用日期：明天
                null,                                // 当天账号：手机A
                null,                                   // 当天密码：密码A
                null,                                 // 昨天账号：手机B
                null,                                    // 昨天密码：密码B
                null,                                       // 当前使用者
                null                                        // 当前使用时间
            )
           ,
            
            // 虚拟卡4 - 
            createVirtualCard("VC004", "备用卡1", "2025.10.01;2025.10.02", 1, 1, 
            	"19397319339", "Aa123456", "13400784761", "Aa123456", "1;3;5;7", 1, null,
                // 新增字段
                currentDate.plusDays(1),                    // 使用日期：明天
                null,                                // 当天账号：手机A
                null,                                   // 当天密码：密码A
                null,                                 // 昨天账号：手机B
                null,                                    // 昨天密码：密码B
                null,                                       // 当前使用者
                null                                        // 当前使用时间
            )
           
        );
        
        virtualCardRepository.saveAll(virtualCards);
        System.out.println("虚拟卡数据初始化完成 - 包含新增字段");
    }
    
    private void initializeGroups() {
        List<Group> groups = Arrays.asList(
            // 分组1 - 技术部
            createGroup("GRP001", "中山公园-沙田-华强南-沙田-中山公园-沙田", 60, "VC001", "ACTIVE"),
            
            // 分组2 - 市场部
            createGroup("GRP002", "技术大学-科学馆-借用", 60, "VC002", "ACTIVE"),
            
            // 分组3 - 研发团队（未激活）
            createGroup("GRP003", "梅林关-长圳-元芬-科学馆", 60, "VC003", "ACTIVE"),
            
            // 分组4 - 测试组
            createGroup("GRP004", "备用组1", 60, "VC004", "ACTIVE")
        );
        
        groupRepository.saveAll(groups);
        System.out.println("分组数据初始化完成");
    }
    
    private void initializeGroupMembers() {
        List<GroupMember> groupMembers = Arrays.asList(
            // GRP001 组成员
            createGroupMember("GRP001", "USER001", "地铁客户-微信-Belle", 14, 60, "06:00", "23:00", "ACTIVE"),
            createGroupMember("GRP001", "USER002", "地铁客户-闲鱼-ljj", 2, 60, "06:00", "23:00", "ACTIVE"),
            createGroupMember("GRP001", "USER003", "地铁客户-闲鱼-今夜外卖", 3, 60, "06:00", "23:00", "ACTIVE"),
            createGroupMember("GRP001", "USER004", "地铁客户-闲鱼-Andy", 5, 60, "06:00", "23:00", "ACTIVE"),
            
            // GRP002 组成员
            createGroupMember("GRP002", "USER005", "地铁客户-闲鱼-用户-长期通勤", 1, null, "06:00", "23:00", "ACTIVE"),
            createGroupMember("GRP002", "USER006", "USER006", 2, 40, "06:00", "23:00", "ACTIVE"),
            createGroupMember("GRP002", "USER007", "USER007", 3, 45, "06:00", "23:00", "ACTIVE"),
            
            // GRP003 组成员（未激活分组）
            createGroupMember("GRP003", "USER008", "豆豆不哭", 14, 30, "07:00", "22:00", "ACTIVE"),
            createGroupMember("GRP003", "USER009", "地铁客户-闲鱼-小玲", 25, 35, "07:00", "22:00", "ACTIVE"),
            createGroupMember("GRP003", "USER010", "地铁客户-闲鱼-是只vip", 36, 35, "07:00", "22:00", "ACTIVE")
            
        );
        
        // 为每个成员生成唯一的访问码和URL
        for (GroupMember member : groupMembers) {
            String accessCode = secureUrlService.generateSimpleFixedAccessCode(member.getUserId(), member.getGroupId());
            member.setAccessCode(accessCode);
            
            String personalUrl = String.format("%s/card.html?uId=%s&gId=%s&code=%s", 
            		Constants.DOMAIN, member.getUserId(), member.getGroupId(), accessCode);
            member.setPersonalUrl(personalUrl);
        }
        
        groupMemberRepository.saveAll(groupMembers);
        System.out.println("组成员数据初始化完成 - 已生成安全访问URL");
    }
    
    private VirtualCard createVirtualCard(String id, String cardName, String notAvailableDates, Integer status, 
                                         Integer periodStatus, String phoneA, String pwdA, 
                                         String phoneB, String pwdB, String usageRule, Integer currentStatus, 
                                         String currentUsedBy) {
        return createVirtualCard(id, cardName, notAvailableDates, status, periodStatus, phoneA, pwdA, phoneB, pwdB, 
                               usageRule, currentStatus, currentUsedBy, null, null, null, null, null, null, null);
    }
    
    private VirtualCard createVirtualCard(String id, String cardName, String notAvailableDates, Integer status, 
                                         Integer periodStatus, String phoneA, String pwdA, 
                                         String phoneB, String pwdB, String usageRule, 
                                         Integer currentStatus, String currentUsedBy,
                                         // 新增字段
                                         LocalDate usageDate, String todayAccount, String todayPassword,
                                         String yesterdayAccount, String yesterdayPassword,
                                         String currentUsedByField, LocalDateTime currentUsageStartTime) {
        VirtualCard card = new VirtualCard();
        // 原有字段
        card.setId(id);
        card.setCardName(cardName);
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
            card.setCurrentUsageStartTime(getBeijingTime().minusMinutes(10)); // 假设已经开始使用10分钟
        }
        
        // 新增字段
        card.setUsageDate(usageDate);
        card.setTodayAccount(todayAccount);
        card.setTodayPassword(todayPassword);
        card.setYesterdayAccount(yesterdayAccount);
        card.setYesterdayPassword(yesterdayPassword);
        
        // 使用传入的当前使用者字段（如果提供）
        if (currentUsedByField != null) {
            card.setCurrentUsedBy(currentUsedByField);
        }
        
        // 使用传入的当前使用时间（如果提供）
        if (currentUsageStartTime != null) {
            card.setCurrentUsageStartTime(currentUsageStartTime);
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
        group.setCreateTime(getBeijingTime());
        group.setStatus(status);
        return group;
    }
    
    private GroupMember createGroupMember(String groupId, String userId, String userName, Integer useOrder, 
                                        Integer customInterval, String startTime, String endTime, 
                                        String status) {
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setUserName(userName);
        member.setUseOrder(useOrder);
        member.setCustomInterval(customInterval);
        member.setUserCardStartTime(startTime);
        member.setUserCardEndTime(endTime);
        member.setStatus(status);
        return member;
    }
    
    public void initSampleUrlData() {
        try { 
            // 创建示例数据
            List<TicketCard> sampleCards = Arrays.asList(
                createSampleCard(1, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=DRVDCW6LBYWRTHBI", "已分配",0),
                createSampleCard(2, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=WLNPORWN1UO8PRCG", "已分配",5),
                createSampleCard(3, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=N1JHM7X96CBSINWE", "可用",5),
                createSampleCard(4, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=C6FVGXJMV9HVVGXE", "可用",10),
                createSampleCard(5, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=GSINEXTL2QZZ51RL", "可用",2),
                createSampleCard(6, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=MFDFEEHUOTEJXHVB", "可用",2),
                createSampleCard(7, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=OTJCWC5AJZ7255D8", "可用",2),
                createSampleCard(8, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=7SRE8ROYWSIILAFL", "可用",2),
                createSampleCard(9, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=ON0KCWDCJ7AOFBZG", "可用",2)
                
            );
            
            List<TicketCard> savedCards = new ArrayList<>();
            for (TicketCard card : sampleCards) {
                try {
                    TicketCard savedCard = ticketCardService.createTicketCard(card);
                    savedCards.add(savedCard);
                } catch (Exception e) {
                    // 如果序号已存在，跳过
                    System.out.println("跳过重复序号: " + card.getSerialNumber());
                }
            }
            
        } catch (Exception e) {
            
        }
    }
    
    private TicketCard createSampleCard(Integer serialNumber, String cardUrl, String status,int count) {
        TicketCard card = new TicketCard();
        card.setSerialNumber(serialNumber);
        card.setCardUrl(cardUrl);
        card.setUsageCount(count);
        card.setStatus(status);
        card.setCreatedTime(LocalDateTime.now());
        return card;
    }
}
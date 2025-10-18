package com.grace.ticket.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grace.ticket.dto.CardCredentials;
import com.grace.ticket.dto.CardInfo;
import com.grace.ticket.entity.Group;
import com.grace.ticket.entity.GroupMember;
import com.grace.ticket.entity.VirtualCard;
import com.grace.ticket.repository.GroupMemberRepository;
import com.grace.ticket.repository.GroupRepository;
import com.grace.ticket.repository.VirtualCardRepository;

import jakarta.transaction.Transactional;

//VirtualCardService.java
@Service
@Transactional
public class VirtualCardService {
 
 @Autowired
 private VirtualCardRepository virtualCardRepository;
 
 @Autowired
 private GroupRepository groupRepository;
 
 @Autowired
 private GroupMemberRepository groupMemberRepository;
 
 
 // 获取北京时间
 private LocalDateTime getBeijingTime() {
     return LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
 }
 
 private LocalDate getBeijingDate() {
     return LocalDate.now(ZoneId.of("Asia/Shanghai"));
 }
 
 /**
  * 重置虚拟卡到初始状态
  */
 public boolean resetToInitialState(String id) {
     try {
         Optional<VirtualCard> optionalCard = virtualCardRepository.findById(id);
         if (optionalCard.isPresent()) {
             VirtualCard card = optionalCard.get();
             
             // 获取当前北京时间
             LocalDate currentDate = getBeijingDate();
             
             // 1. 使用日期设置为当前日期 + 1天
             card.setUsageDate(currentDate.plusDays(1));
             
             // 2. 将当天账号密码挪到昨天账号密码
             card.setYesterdayAccount(card.getTodayAccount());
             card.setYesterdayPassword(card.getTodayPassword());
             
             // 3. 根据usageRule设置新的当天账号密码
             String todayAccount;
             String todayPassword;
             
             if (shouldUseCardA(currentDate, card.getUsageRule())) {
                 // 使用手机A和密码A
                 todayAccount = card.getPhysicalCardAPhone();
                 todayPassword = card.getPhysicalCardAPwd();
             } else {
                 // 使用手机B和密码B
                 todayAccount = card.getPhysicalCardBPhone();
                 todayPassword = card.getPhysicalCardBPwd();
             }
             
             card.setTodayAccount(todayAccount);
             card.setTodayPassword(todayPassword);
             
             // 4. 重置当前状态为0
             
             
             List<Group> groups= groupRepository.findAll();
             
             String groupId="";
             for(Group g:groups) {
            	 if(g.getVirtualCardId().equals(card.getId())) {
            		 groupId=g.getGroupId();
            		 break;
            	 }
             }
             
             
          // 获取当前组的所有成员
             List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(groupId);
             
             // 获取下一个用户的使用序号
             int nextUserOrder = 1;
             
             while(TicketUtilService.isNextUserOnLeave(groupMembers, nextUserOrder)) {
             	nextUserOrder ++;
             }
             
             card.setCurrentStatus(nextUserOrder);
             card.setPeriodStatus(nextUserOrder);
             
             virtualCardRepository.save(card);
             return true;
         }
         return false;
     } catch (Exception e) {
         throw new RuntimeException("重置虚拟卡失败: " + e.getMessage());
     }
 }
 
 /**
  * 判断是否应该使用卡A
  * usageRule格式如："1;3;5;7" 表示1,3,5,7使用卡A，其他日期使用卡B
  */
 private boolean shouldUseCardA(LocalDate date, String usageRule) {
     if (usageRule == null || usageRule.trim().isEmpty()) {
         // 如果没有设置规则，默认使用卡A
         return true;
     }
     
     try {
         // 获取星期几（1=星期一, 7=星期日）
         int dayOfWeek = date.getDayOfWeek().getValue();
         
         // 解析规则
         String[] rules = usageRule.split(";");
         for (String rule : rules) {
             if (rule.trim().equals(String.valueOf(dayOfWeek))) {
                 return true;
             }
         }
         return false;
     } catch (Exception e) {
         // 解析失败时默认使用卡A
         return true;
     }
 }
 
 public List<VirtualCard> findAll() {
     return virtualCardRepository.findAll();
 }
 
 public Optional<VirtualCard> findById(String id) {
     return virtualCardRepository.findById(id);
 }
 
 public VirtualCard save(VirtualCard virtualCard) {
     return virtualCardRepository.save(virtualCard);
 }
 
 public void deleteById(String id) {
     virtualCardRepository.deleteById(id);
 }
 
 public VirtualCard getVirtualCardByGroupId(String groupId) {
     return virtualCardRepository.findByGroupId(groupId)
             .orElseThrow(() -> new RuntimeException("未找到该分组对应的虚拟乘车卡"));
 }
 
 /**
  * 获取卡片信息（包含使用日期）
  */
 public CardInfo getCardInfo(String groupId) {
     VirtualCard virtualCard = getVirtualCardByGroupId(groupId);
     CardCredentials credentials = virtualCard.getCurrentCredentials();
     
     return CardInfo.builder() 
             .virtualCardId(virtualCard.getId())
             .phone(credentials.getPhone())
             .password(credentials.getPassword())
             .status(virtualCard.getStatus())
             .currentStatus(virtualCard.getCurrentStatus()) // 修正：使用 currentStatus 而不是 periodStatus
             .currentUsedBy(virtualCard.getCurrentUsedBy())
             .todayAvailable(virtualCard.isTodayAvailable())
             .periodAvailable(virtualCard.isPeriodAvailable())
             .usageDate(virtualCard.getUsageDate()) // 新增：添加使用日期
             .periodStatus(virtualCard.getPeriodStatus())
             .build();
 }
 
 /**
  * 获取卡片信息（包含时间逻辑的账号密码）
  */
 public CardInfo getCardInfoWithTimeLogic(String groupId, String userId, String passwordSpecialTime) {
     VirtualCard virtualCard = getVirtualCardByGroupId(groupId); 
     
     // 根据时间逻辑决定使用当天还是昨天的账号密码
     CardCredentials credentials = getCredentialsByTimeLogic(virtualCard, passwordSpecialTime);
     
     return CardInfo.builder()
             .virtualCardId(virtualCard.getId())
             .phone(credentials.getPhone())
             .password(credentials.getPassword())
             .status(virtualCard.getStatus())
             .currentStatus(virtualCard.getCurrentStatus())
             .currentUsedBy(virtualCard.getCurrentUsedBy())
             .todayAvailable(virtualCard.isTodayAvailable())
             .periodAvailable(virtualCard.isPeriodAvailable())
             .usageDate(virtualCard.getUsageDate())
             .periodStatus(virtualCard.getPeriodStatus())
             .build();
 }
 
 /**
  * 根据时间逻辑获取账号密码
  */
 private CardCredentials getCredentialsByTimeLogic(VirtualCard virtualCard, String passwordSpecialTime) {
     if (passwordSpecialTime == null || passwordSpecialTime.trim().isEmpty()) {
         // 没有设置特殊时间，使用当天账号密码
         return new CardCredentials(virtualCard.getTodayAccount(), virtualCard.getTodayPassword());
     }
     
     try {
         // 解析特殊时间（格式: "07:00"）
         String[] timeParts = passwordSpecialTime.split(":");
         if (timeParts.length != 2) {
             return new CardCredentials(virtualCard.getTodayAccount(), virtualCard.getTodayPassword());
         }
         
         int specialHour = Integer.parseInt(timeParts[0]);
         int specialMinute = Integer.parseInt(timeParts[1]);
         
         // 获取当前时间（北京时间）
         LocalTime currentTime = LocalTime.now(ZoneId.of("Asia/Shanghai"));
         LocalTime specialTime = LocalTime.of(specialHour, specialMinute);
         
         // 如果当前时间早于特殊时间，使用昨天账号密码
         if (currentTime.isBefore(specialTime)) {
             // 如果昨天账号密码为空，则使用当天账号密码
             String account = virtualCard.getYesterdayAccount() != null ? 
                 virtualCard.getYesterdayAccount() : virtualCard.getTodayAccount();
             String password = virtualCard.getYesterdayPassword() != null ? 
                 virtualCard.getYesterdayPassword() : virtualCard.getTodayPassword();
             return new CardCredentials(account, password);
         } else {
             // 使用当天账号密码
             return new CardCredentials(virtualCard.getTodayAccount(), virtualCard.getTodayPassword());
         }
         
     } catch (Exception e) {
         System.err.println("解析密码特殊时间失败: " + passwordSpecialTime);
         return new CardCredentials(virtualCard.getTodayAccount(), virtualCard.getTodayPassword());
     }
 }
 
 public boolean canUserUseCard(String groupId, String userId, Integer userOrder) {
     VirtualCard virtualCard = getVirtualCardByGroupId(groupId);
     
     // 检查票卡是否可用
     if (!virtualCard.isAvailable()) {
         return false;
     }
     
     // 检查今天是否可用
     if (!virtualCard.isTodayAvailable()) {
         return false;
     }
     
     // 检查当前状态是否与用户序号匹配
     if (!virtualCard.getCurrentStatus().equals(userOrder)) {
         return false;
     }
     
     // 检查是否已被其他用户使用
     if (virtualCard.getCurrentUsedBy() != null && !virtualCard.getCurrentUsedBy().equals(userId)) {
         return false;
     }
     
     return true;
 }
 
 /**
  * 初始化虚拟卡的使用日期（如果为空）
  */
 public void initializeUsageDateIfNull(String virtualCardId) {
     Optional<VirtualCard> optionalCard = virtualCardRepository.findById(virtualCardId);
     if (optionalCard.isPresent()) {
         VirtualCard card = optionalCard.get();
         if (card.getUsageDate() == null) {
             // 如果没有设置使用日期，设置为当前日期+1天
             card.setUsageDate(getBeijingDate().plusDays(1));
             virtualCardRepository.save(card);
             System.out.println("初始化虚拟卡 " + virtualCardId + " 的使用日期: " + card.getUsageDate());
         }
     }
 }
 
 /**
  * 批量初始化所有虚拟卡的使用日期
  */
 public void initializeAllUsageDates() {
     List<VirtualCard> allCards = virtualCardRepository.findAll();
     for (VirtualCard card : allCards) {
         if (card.getUsageDate() == null) {
             card.setUsageDate(getBeijingDate().plusDays(1));
             virtualCardRepository.save(card);
             System.out.println("初始化虚拟卡 " + card.getId() + " 的使用日期: " + card.getUsageDate());
         }
     }
 }
}
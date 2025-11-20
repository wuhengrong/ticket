package com.grace.ticket.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grace.ticket.dto.TicketSearchRequest;
import com.grace.ticket.dto.TicketSearchResponse;
import com.grace.ticket.dto.UseTicketRequest;
import com.grace.ticket.entity.VipCard;
import com.grace.ticket.entity.VipCustomer;
import com.grace.ticket.entity.VipRecord;
import com.grace.ticket.repository.VipCardRepository;
import com.grace.ticket.repository.VipCustomerRepository;
import com.grace.ticket.repository.VipRecordRepository;
import com.grace.ticket.risk.VipCardValidator;
import com.grace.ticket.util.DateTimeUtils;

@Service
public class VipCardService {
    
    @Autowired
    private VipCardRepository vipCardRepository;
    
    @Autowired
    private VipCustomerRepository vipCustomerRepository;
    
    @Autowired
    private VipRecordRepository vipRecordRepository;
    
    @Autowired
    private VipCardValidator vipCardValidator;
    
    
    public boolean isNextMorning(LocalDateTime lastCheckTime) {
        LocalDateTime now = LocalDateTime.now();
        
        LocalDateTime fiveHoursLater = lastCheckTime.plusHours(5);
        
        // 检查是否跨天，大于5个小时以上
        boolean crossedMidnight = now.toLocalDate().isAfter(lastCheckTime.toLocalDate()) && now.isAfter(fiveHoursLater);
        
        // 检查当前时间是否在早上范围内
        LocalTime currentTime = now.toLocalTime();
        LocalTime morningStart = LocalTime.of(6, 0);
        LocalTime morningEnd = LocalTime.of(10, 0);
        
        return crossedMidnight && 
               currentTime.isAfter(morningStart) && 
               currentTime.isBefore(morningEnd);
    }
    
    
    
    /**
     * 搜索最佳匹配票卡,旧方法，备用
     */
    @Transactional(readOnly = true)
    public TicketSearchResponse searchBestMatchCard2(TicketSearchRequest request) {
        try {
            // 验证VIP客户
            Optional<VipCustomer> customerOpt = vipCustomerRepository.findById(request.getVipCustomerId());
            if (customerOpt.isEmpty()) {
                return TicketSearchResponse.failure("VIP客户不存在");
            }
            
            VipCustomer customer = customerOpt.get();
            if (customer.getRideCount() <= 0) {
                return TicketSearchResponse.failure("次卡次数不足");
            }
            
            
            
         // 首先检查用户是否有预定的VIP卡（状态为RESERVED）
            List<VipCard> reservedCards = vipCardRepository.findReservedCardsByUserName(customer.getUserName());
            if (!reservedCards.isEmpty()) {
                // 找到用户预定的卡片，直接返回第一个
            	
            	for(VipCard reservedCard:reservedCards) {
            		//保留票非新票
            		if(null!=reservedCard.getExpiryTime() && null!= request.getBoardingTime() && reservedCard.getExpiryTime().isAfter(request.getBoardingTime())) {
            			 // 计算预估出站时间
                        LocalDateTime estimatedTime = vipCardValidator.calculateEstimatedAlightingTime(
                            request.getBoardingStation(),
                            request.getAlightingStation(),
                            request.getBoardingTime()
                        );
                        
                        return TicketSearchResponse.success(
                            new com.grace.ticket.dto.VipCardDTO(reservedCard),
                            estimatedTime,
                            customer.getRideCount()
                        );
            		} else if(null==reservedCard.getExpiryTime()) { //新票
            			LocalDateTime estimatedTime = vipCardValidator.calculateEstimatedAlightingTime(
                                request.getBoardingStation(),
                                request.getAlightingStation(),
                                request.getBoardingTime()
                            );
                            
                            return TicketSearchResponse.success(
                                new com.grace.ticket.dto.VipCardDTO(reservedCard),
                                estimatedTime,
                                customer.getRideCount()
                            );
            		}
            	}
                
               
            }
            
            
            
            
            // 获取可用票卡
            List<VipCard> availableCards = vipCardRepository.findAvailableCards(DateTimeUtils.now()); 
            if (availableCards.isEmpty()) {
                return TicketSearchResponse.failure("暂无可用票"); 
            }
            
            
            
            
            
            
            // 查找最佳匹配票卡
            Optional<VipCard> bestMatch = vipCardValidator.findBestMatchCard(
                availableCards, 
                request.getBoardingStation(), 
                request.getAlightingStation(), 
                request.getBoardingTime()
            );
            
            if (bestMatch.isEmpty()) {
                return TicketSearchResponse.failure("无匹配票卡");
            }
            
            VipCard matchedCard = bestMatch.get();
            
            // 计算预估出站时间
            LocalDateTime estimatedTime = vipCardValidator.calculateEstimatedAlightingTime(
                request.getBoardingStation(),
                request.getAlightingStation(),
                request.getBoardingTime()
            );
            
            return TicketSearchResponse.success(
                new com.grace.ticket.dto.VipCardDTO(matchedCard),
                estimatedTime,
                customer.getRideCount()
            );
            
        } catch (Exception e) {
            return TicketSearchResponse.failure("查询失败: " + e.getMessage());
        }
    }
    
    
    /**
     * 搜索最佳匹配票卡
     */
    /**
     * 搜索最佳匹配票卡
     */
    @Transactional(readOnly = true)
    public TicketSearchResponse searchBestMatchCard(TicketSearchRequest request) {
        try {
            // 验证VIP客户
            Optional<VipCustomer> customerOpt = vipCustomerRepository.findById(request.getVipCustomerId());
            if (customerOpt.isEmpty()) {
                return TicketSearchResponse.failure("VIP客户不存在");
            }
            
            VipCustomer customer = customerOpt.get();
            if (customer.getRideCount() <= 0) {
                return TicketSearchResponse.failure("次卡次数不足");
            }
            
            // 首先检查用户是否有预定的VIP卡（状态为RESERVED）
            List<VipCard> reservedCards = vipCardRepository.findReservedCardsByUserName(customer.getUserName());
            if (!reservedCards.isEmpty()) {
                for(VipCard reservedCard : reservedCards) {
                    if(null != reservedCard.getExpiryTime() && null != request.getBoardingTime() && reservedCard.getExpiryTime().isAfter(request.getBoardingTime())) {
                        LocalDateTime estimatedTime = vipCardValidator.calculateEstimatedAlightingTime(
                            request.getBoardingStation(),
                            request.getAlightingStation(),
                            request.getBoardingTime()
                        );
                        
                        return TicketSearchResponse.success(
                            new com.grace.ticket.dto.VipCardDTO(reservedCard),
                            estimatedTime,
                            customer.getRideCount()
                        );
                    } else if(null == reservedCard.getExpiryTime()) { //新票
                        LocalDateTime estimatedTime = vipCardValidator.calculateEstimatedAlightingTime(
                            request.getBoardingStation(),
                            request.getAlightingStation(),
                            request.getBoardingTime()
                        );
                        
                        return TicketSearchResponse.success(
                            new com.grace.ticket.dto.VipCardDTO(reservedCard),
                            estimatedTime,
                            customer.getRideCount()
                        );
                    }
                }
            }
            
            // 获取可用票卡
            List<VipCard> availableCards = vipCardRepository.findAvailableCards(DateTimeUtils.now()); 
            
            // 根据客户类型进行卡片筛选
            TicketSearchResponse filteredResponse = filterCardsByCustomerType(availableCards, customer, request);
            if (filteredResponse != null) {
                return filteredResponse;
            }
            
            // 如果经过客户类型筛选后没有返回结果，继续执行通用规则
            return applyCardSelectionRules(availableCards, customer, request);
            
        } catch (Exception e) {
            return TicketSearchResponse.failure("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据客户类型筛选卡片
     */
    /**
     * 根据客户类型筛选卡片
     */
    private TicketSearchResponse filterCardsByCustomerType(List<VipCard> availableCards, 
                                                          VipCustomer customer, 
                                                          TicketSearchRequest request) {
        String customerType = customer.getCustomerType();
        
        switch (customerType) {
            case VipCustomer.CustomerType.GENERAL: // 一般 - 只能使用链接次卡
                return TicketSearchResponse.failure("无匹配票卡");
                
            case VipCustomer.CustomerType.LIMITED: // 限制 - 特定时间用过的次卡 + 链接次卡
                LocalTime currentTime = LocalTime.now();
                LocalTime morningStart = LocalTime.of(6, 0);
                LocalTime morningEnd = LocalTime.of(9, 0);
                
                // 只有在早上6-9点才返回用过的次卡
                if (currentTime.isAfter(morningStart) && currentTime.isBefore(morningEnd)) {
                    // 获取隔夜卡列表
                    List<VipCard> overnightCards = getOvernightCards(availableCards);
                    if (!overnightCards.isEmpty()) {
                        // 返回第一条隔夜卡
                        VipCard firstOvernightCard = overnightCards.get(0);
                        return createSuccessResponse(firstOvernightCard, customer, request);
                    } else {
                        return TicketSearchResponse.failure("无可用次卡");
                    }
                } else {
                    return TicketSearchResponse.failure("无匹配票卡");
                }
                
            case VipCustomer.CustomerType.NORMAL: // 普通 - 只能用已经用过的次卡
                // 过滤出用过的次卡（expiryTime不为空）
                List<VipCard> usedCards = availableCards.stream()
                    .filter(card -> card.getExpiryTime() != null)
                    .collect(Collectors.toList());
                    
                if (usedCards.isEmpty()) {
                    return TicketSearchResponse.failure("无可用次卡");
                }
                // 继续使用用过的次卡执行通用规则
                return applyCardSelectionRules(usedCards, customer, request);
                
            case VipCustomer.CustomerType.PLATINUM: // 白金 - 用过的次卡 + 新的次卡
                // 使用所有可用卡片执行通用规则
                return applyCardSelectionRules(availableCards, customer, request);
                
            case VipCustomer.CustomerType.BLACK: // 黑金 - 白金 + 备用卡
                // 获取备用卡并合并到可用卡片中
                List<VipCard> reservedCards = vipCardRepository.findStandbyCards();
                List<VipCard> allCards = new ArrayList<>(availableCards);
                allCards.addAll(reservedCards);
                return applyCardSelectionRules(allCards, customer, request);
                
            default:
                // 默认使用所有可用卡片执行通用规则
                return applyCardSelectionRules(availableCards, customer, request);
        }
    }

    /**
     * 应用卡片选择规则
     */
    private TicketSearchResponse applyCardSelectionRules(List<VipCard> cards, 
                                                       VipCustomer customer, 
                                                       TicketSearchRequest request) {
        if (cards.isEmpty()) {
            return getFailureMessageByCustomerType(customer.getCustomerType());
        }
        
        // 1. 隔夜卡列表
        List<VipCard> overnightCards = getOvernightCards(cards);
        if (!overnightCards.isEmpty()) {
            VipCard firstOvernightCard = overnightCards.get(0);
            return createSuccessResponse(firstOvernightCard, customer, request);
        }
        
        // 2. 今日已用卡列表
        List<VipCard> todayUsedCards = getTodayUsedCards(cards); 
        if (!todayUsedCards.isEmpty()) {
            Optional<VipCard> bestMatch = vipCardValidator.findBestMatchCard(
                todayUsedCards, 
                request.getBoardingStation(), 
                request.getAlightingStation(), 
                request.getBoardingTime()
            );
            if (bestMatch.isPresent()) {
                return createSuccessResponse(bestMatch.get(), customer, request);
            }
        }
        
        // 3. 可用新卡列表
        List<VipCard> newCards = getNewCards(cards);
        if (!newCards.isEmpty()) {
            VipCard firstNewCard = newCards.get(0);
            return createSuccessResponse(firstNewCard, customer, request);
        }
        
        // 4. 黑金卡列表（备用卡）- 只有在黑金客户类型时才检查
        if (VipCustomer.CustomerType.BLACK.equals(customer.getCustomerType())) {
            List<VipCard> reservedCards = vipCardRepository.findReservedCards();
            if (!reservedCards.isEmpty()) {
                VipCard firstReservedCard = reservedCards.get(0);
                return createSuccessResponse(firstReservedCard, customer, request);
            }
        }
        
        return getFailureMessageByCustomerType(customer.getCustomerType());
    }

    /**
     * 获取隔夜卡列表
     */
    private List<VipCard> getOvernightCards(List<VipCard> cards) {
        return cards.stream()
            .filter(card -> {
                if (card.getAlightingTime() == null) {
                    return false;
                }
                // 使用isNextMorning判断是否为隔夜卡
                return isNextMorning(card.getAlightingTime());
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取今日已用卡列表
     */
    private List<VipCard> getTodayUsedCards(List<VipCard> cards) {
        // 获取隔夜卡列表
        List<VipCard> overnightCards = getOvernightCards(cards);
        
        return cards.stream()
            .filter(card -> card.getExpiryTime() != null) // 用过的卡（expiryTime不为空）
            .filter(card -> !overnightCards.contains(card)) // 排除隔夜卡
            .collect(Collectors.toList());
    }

    /**
     * 获取可用新卡列表
     */
    private List<VipCard> getNewCards(List<VipCard> cards) {
        return cards.stream()
            .filter(card -> card.getExpiryTime() == null) // expiryTime为空
            .collect(Collectors.toList());
    }

    /**
     * 创建成功响应
     */
    private TicketSearchResponse createSuccessResponse(VipCard card, VipCustomer customer, TicketSearchRequest request) {
        LocalDateTime estimatedTime = vipCardValidator.calculateEstimatedAlightingTime(
            request.getBoardingStation(),
            request.getAlightingStation(),
            request.getBoardingTime()
        );
        
        return TicketSearchResponse.success(
            new com.grace.ticket.dto.VipCardDTO(card),
            estimatedTime,
            customer.getRideCount()
        );
    }


    /**
     * 判断是否为隔夜卡（基于alightingTime判断是否隔天早晨）
     */
    public boolean isNextMorning2(LocalDateTime lastCheckTime) {
        if (lastCheckTime == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 检查是否跨天
        boolean crossedMidnight = now.toLocalDate().isAfter(lastCheckTime.toLocalDate());
        
        // 检查当前时间是否在早上范围内（6-10点）
        LocalTime currentTime = now.toLocalTime();
        LocalTime morningStart = LocalTime.of(6, 0);
        LocalTime morningEnd = LocalTime.of(10, 0);
        
        // 检查时间差是否大于5小时
        LocalDateTime fiveHoursLater = lastCheckTime.plusHours(5);
        boolean moreThan5Hours = now.isAfter(fiveHoursLater);
        
        return crossedMidnight && 
               currentTime.isAfter(morningStart) && 
               currentTime.isBefore(morningEnd) &&
               moreThan5Hours;
    }

    /**
     * 根据客户类别和早上规则筛选卡片
     */
    private List<VipCard> filterCardsByCustomerTypeAndMorningRule(List<VipCard> availableCards, 
                                                                 VipCustomer customer, 
                                                                 LocalDateTime boardingTime) {
        String customerType = customer.getCustomerType();
        boolean isMorning = isNextMorning(DateTimeUtils.now()); // 使用当前时间判断
        
        System.out.println("客户类别: " + customerType + ", 是否隔天早晨: " + isMorning); 
        
        // 如果是隔天早晨，返回第一个有效时间的VipCard
        if (isMorning && !availableCards.isEmpty()) {
            VipCard firstCard = availableCards.get(0);
            return List.of(firstCard); // 返回第一个有效卡片
        }
        
        // 根据客户类别筛选卡片
        switch (customerType) {
            case VipCustomer.CustomerType.GENERAL: // 一般 - 只能使用链接次卡
                return List.of(); // 返回空列表，强制使用链接次卡
                
            case VipCustomer.CustomerType.LIMITED: // 限制 - 特定时间用过的次卡 + 链接次卡
                // 如果在早上6-9点，返回用过的次卡，否则返回空列表
                LocalTime currentTime = LocalTime.now();
                LocalTime morningStart = LocalTime.of(6, 0);
                LocalTime morningEnd = LocalTime.of(9, 0);
                
                if (currentTime.isAfter(morningStart) && currentTime.isBefore(morningEnd)) {
                    // 返回用过的次卡（expiryTime不为空）
                    return availableCards.stream()
                        .filter(card -> card.getExpiryTime() != null)
                        .collect(Collectors.toList());
                } else {
                    return List.of(); // 非早上时段，返回空列表
                }
                
            case VipCustomer.CustomerType.NORMAL: // 普通 - 只能用已经用过的次卡
                return availableCards.stream()
                    .filter(card -> card.getExpiryTime() != null) // expiryTime不为空
                    .collect(Collectors.toList());
                    
            case VipCustomer.CustomerType.PLATINUM: // 白金 - 用过的次卡 + 新的次卡
                return availableCards; // 返回所有可用卡片
                
            case VipCustomer.CustomerType.BLACK: // 黑金 - 白金 + 备用卡
                // 获取备用卡
                List<VipCard> reservedCards = vipCardRepository.findReservedCards();
                List<VipCard> allCards = new ArrayList<>(availableCards);
                allCards.addAll(reservedCards);
                return allCards;
                
            default:
                return availableCards; // 默认返回所有可用卡片
        }
    }

    /**
     * 根据客户类别返回对应的失败消息
     */
    private TicketSearchResponse getFailureMessageByCustomerType(String customerType) {
        switch (customerType) {
            case VipCustomer.CustomerType.GENERAL:
                return TicketSearchResponse.failure("无匹配票卡"); // 一般客户直接返回无匹配
                
            case VipCustomer.CustomerType.LIMITED:
                LocalTime currentTime = LocalTime.now();
                LocalTime morningStart = LocalTime.of(6, 0);
                LocalTime morningEnd = LocalTime.of(9, 0);
                
                if (currentTime.isAfter(morningStart) && currentTime.isBefore(morningEnd)) {
                    return TicketSearchResponse.failure("无可用次卡");
                } else {
                    return TicketSearchResponse.failure("无匹配票卡"); // 非早上时段
                }
                
            case VipCustomer.CustomerType.NORMAL:
                return TicketSearchResponse.failure("无可用次卡");
                
            case VipCustomer.CustomerType.PLATINUM:
                return TicketSearchResponse.failure("无可用票");
                
            case VipCustomer.CustomerType.BLACK:
                return TicketSearchResponse.failure("无可用票");
                
            default:
                return TicketSearchResponse.failure("暂无可用票");
        }
    }

    /**
     * 修改 isNextMorning 方法，使用当前时间判断
     */
    public boolean isNextMorning() {
        LocalDateTime now = LocalDateTime.now();
        
        // 检查当前时间是否在早上6-10点范围内
        LocalTime currentTime = now.toLocalTime();
        LocalTime morningStart = LocalTime.of(6, 0);
        LocalTime morningEnd = LocalTime.of(10, 0);
        
        return currentTime.isAfter(morningStart) && currentTime.isBefore(morningEnd);
    }
    
    
    /**
     * 搜索最佳匹配票卡
     */
    @Transactional(readOnly = true)
    public TicketSearchResponse searchBestMatchCardForVIP(TicketSearchRequest request) {
        try {
            // 验证VIP客户
            Optional<VipCustomer> customerOpt = vipCustomerRepository.findById(request.getVipCustomerId());
            if (customerOpt.isEmpty()) {
                return TicketSearchResponse.failure("VIP客户不存在");
            }
            
            VipCustomer customer = customerOpt.get();
            if (customer.getRideCount() <= 0) {
                return TicketSearchResponse.failure("次卡次数不足");
            }
            
            
         // 首先检查用户是否有预定的VIP卡（状态为RESERVED）
            List<VipCard> reservedCards = vipCardRepository.findReservedCardsByUserName(customer.getUserName());
            if (!reservedCards.isEmpty()) {
                // 找到用户预定的卡片，直接返回第一个
            	
            	for(VipCard reservedCard:reservedCards) {
            		if(null!=reservedCard.getExpiryTime() && null!= request.getBoardingTime() && reservedCard.getExpiryTime().isAfter(request.getBoardingTime())) {
            			 // 计算预估出站时间
                        LocalDateTime estimatedTime = vipCardValidator.calculateEstimatedAlightingTime(
                            request.getBoardingStation(),
                            request.getAlightingStation(),
                            request.getBoardingTime()
                        );
                        
                        return TicketSearchResponse.success(
                            new com.grace.ticket.dto.VipCardDTO(reservedCard),
                            estimatedTime,
                            customer.getRideCount()
                        );
            		}
            	}
                
               
            }
            
            // 获取可用票卡
            List<VipCard> availableCards = vipCardRepository.findAvailableCards(DateTimeUtils.now()); 
            if (availableCards.isEmpty()) {
                return TicketSearchResponse.failure("无可用票,点击如下按钮获取连接二维码乘车"); 
            }
            
            // 查找最佳匹配票卡
            Optional<VipCard> bestMatch = vipCardValidator.findBestMatchCard(
                availableCards, 
                request.getBoardingStation(), 
                request.getAlightingStation(), 
                request.getBoardingTime()
            );
            
            if (bestMatch.isEmpty()) {
                return TicketSearchResponse.failure("无匹配票卡");
            }
            
            VipCard matchedCard = bestMatch.get();
            
            // 计算预估出站时间
            LocalDateTime estimatedTime = vipCardValidator.calculateEstimatedAlightingTime(
                request.getBoardingStation(),
                request.getAlightingStation(),
                request.getBoardingTime()
            );
            
            return TicketSearchResponse.success(
                new com.grace.ticket.dto.VipCardDTO(matchedCard),
                estimatedTime,
                customer.getRideCount()
            );
            
        } catch (Exception e) {
            return TicketSearchResponse.failure("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用票卡
     */
    @Transactional
    public TicketSearchResponse useTicket(UseTicketRequest request) {
        try {
            // 验证VIP客户
            Optional<VipCustomer> customerOpt = vipCustomerRepository.findById(request.getVipCustomerId());
            if (customerOpt.isEmpty()) {
                return TicketSearchResponse.failure("VIP客户不存在");
            }
            
            VipCustomer customer = customerOpt.get();
            if (customer.getRideCount() <= 0) {
                return TicketSearchResponse.failure("次卡次数不足");
            }
            
            // 验证票卡
            Optional<VipCard> cardOpt = vipCardRepository.findById(request.getVipCardId());
            if (cardOpt.isEmpty()) {
                return TicketSearchResponse.failure("票卡不存在");
            }
            
            VipCard card = cardOpt.get();
	         // 允许的状态：AVAILABLE、RESERVED，或者 STANDBY 且客户类型为 SVIP
	         boolean isAvailable = card.getStatus() == VipCard.CardStatus.AVAILABLE 
	                            || card.getStatus() == VipCard.CardStatus.RESERVED
	                            || (card.getStatus() == VipCard.CardStatus.STANDBY && "SVIP".equals(customer.getCustomerType()));
	
	         if (!isAvailable) {
	             return TicketSearchResponse.failure("票卡不可用");
	         }
            // 计算预估出站时间
            LocalDateTime estimatedTime = vipCardValidator.calculateEstimatedAlightingTime(
                request.getBoardingStation(),
                request.getAlightingStation(),
                DateTimeUtils.now()
            );
            
            // 更新票卡状态
            card.setStatus(VipCard.CardStatus.IN_USE);
            card.setInOutStatus(VipCard.InOutStatus.IN);
            card.setBoardingStation(request.getBoardingStation());
            card.setAlightingStation(request.getAlightingStation());
            card.setBoardingTime(DateTimeUtils.now());
            card.setEstimatedAlightingTime(estimatedTime);
            
            //如果为空，判断为初次使用
            if(card.getFirstUseTime()==null) {
            	card.setFirstUseTime(DateTimeUtils.now());
            	card.setExpiryTime( DateTimeUtils.now().plusDays(1));
            }
            
            card.setAlightingTime(null);
            
            // 首次使用时间
            if (card.getFirstUseTime() == null) {
                card.setFirstUseTime(DateTimeUtils.now()); 
            }
            
            vipCardRepository.saveAndFlush(card);
            
            // 扣除次卡次数
            int updated = vipCustomerRepository.decrementRideCount(request.getVipCustomerId());
            if (updated == 0) {
                throw new RuntimeException("扣除次卡次数失败");
            }
            
            // 记录使用日志
            VipRecord record = new VipRecord(  
                request.getVipCustomerId(),
                request.getVipCardId(),
                request.getBoardingStation(), 
                DateTimeUtils.now(),
                request.getAlightingStation(),
                estimatedTime, 
                "IN_PROGRESS"
            );
            record.setEstimatedAlightingTime(estimatedTime);
            vipRecordRepository.saveAndFlush(record);
            
            // 获取更新后的客户信息
            customer = vipCustomerRepository.findById(request.getVipCustomerId()).get();
            
            return TicketSearchResponse.success(
                new com.grace.ticket.dto.VipCardDTO(card),
                estimatedTime,
                customer.getRideCount()
            );
            
        } catch (Exception e) {
            return TicketSearchResponse.failure("使用票卡失败: " + e.getMessage());
        }
    }
    
    
    @Transactional
    public TicketSearchResponse returnTicket(Long vipCardId, Long vipCustomerId, String alightingStation) {
        try {
            // 验证票卡和客户
            Optional<VipCard> cardOpt = vipCardRepository.findById(vipCardId);
            Optional<VipCustomer> customerOpt = vipCustomerRepository.findById(vipCustomerId);
            
            if (cardOpt.isEmpty() || customerOpt.isEmpty()) {
                return TicketSearchResponse.failure("票卡或客户不存在");
            }
            
            VipCard card = cardOpt.get();
            if (card.getStatus() != VipCard.CardStatus.IN_USE) {
                return TicketSearchResponse.failure("票卡未在使用中");
            }
            
            // 使用双重条件查询 - 选择其中一种方法
            List<VipRecord> activeRecords = vipRecordRepository.findActiveRecordsByCardAndCustomer(vipCardId, vipCustomerId);
            
            // 或者使用@Query注解的方法
            // List<VipRecord> activeRecords = vipRecordRepository.findActiveRecordsByCardAndCustomer(vipCardId, vipCustomerId);
            
            if (activeRecords.isEmpty()) {
                return TicketSearchResponse.failure("未找到该客户使用此票卡的记录");
            }
            
            // 更新票卡状态
            card.setStatus(VipCard.CardStatus.AVAILABLE);
            card.setInOutStatus(VipCard.InOutStatus.OUT);
            card.setAlightingStation(alightingStation);
            card.setAlightingTime(DateTimeUtils.now());
            vipCardRepository.saveAndFlush(card);
            
            // 更新使用记录
            VipRecord record = activeRecords.get(0);
            record.setAlightingStation(alightingStation);
            record.setAlightingTime(DateTimeUtils.now());
            record.setStatus("COMPLETED");
            vipRecordRepository.saveAndFlush(record);
            
            return TicketSearchResponse.success(
                    new com.grace.ticket.dto.VipCardDTO(card),
                    null,
                    null
                );
            
        } catch (Exception e) {
            return TicketSearchResponse.failure("归还票卡失败: " + e.getMessage());
        }
    }
    
 // Service层
    @Transactional
    public TicketSearchResponse returnTicket2(Long vipCardId, Long vipCustomerId, String alightingStation) {
        try {
            // 验证票卡和客户
            Optional<VipCard> cardOpt = vipCardRepository.findById(vipCardId);
            Optional<VipCustomer> customerOpt = vipCustomerRepository.findById(vipCustomerId);
            
            if (cardOpt.isEmpty() || customerOpt.isEmpty()) {
                return TicketSearchResponse.failure("票卡或客户不存在");
            }
            
            VipCard card = cardOpt.get();
            if (card.getStatus() != VipCard.CardStatus.IN_USE) {
                return TicketSearchResponse.failure("票卡未在使用中");
            }
            
            // 使用双重条件查询
            List<VipRecord> activeRecords = vipRecordRepository.findActiveRecordsByCardAndCustomer(vipCardId, vipCustomerId);
            if (!activeRecords.isEmpty()) {
                VipRecord record = activeRecords.get(0);
                record.setAlightingStation(alightingStation);
                record.setAlightingTime(DateTimeUtils.now());
                vipRecordRepository.save(record);
            }
            
            return TicketSearchResponse.success(
                new com.grace.ticket.dto.VipCardDTO(card),
                null,
                null
            );
            
        } catch (Exception e) {
            return TicketSearchResponse.failure("归还票卡失败: " + e.getMessage());
        }
    }
    
    
    /**
     * 获取客户历史记录
     */
    @Transactional(readOnly = true)
    public List<VipRecord> getCustomerHistory(Long customerId) {
        return vipRecordRepository.findByVipCustomerIdOrderByBoardingTimeDesc(customerId);
    }
    
    /**
     * 获取客户信息
     */
    @Transactional(readOnly = true)
    public Optional<VipCustomer> getCustomerByVipUrl(String vipUrl) {
        System.out.println("原始VIP URL: " + vipUrl);
        
        // 先尝试直接查询
        Optional<VipCustomer> customer = vipCustomerRepository.findByVipUrl(vipUrl);
        if (customer.isPresent()) {
            System.out.println("直接查询找到客户");
            return customer;
        }
        
        // 如果找不到，尝试将 svip.html 转换为 vip.html
        if (vipUrl.contains("svip.html")) {
            String convertedUrl = vipUrl.replace("svip.html", "vip.html");
            System.out.println("转换后的URL: " + convertedUrl);
            
            customer = vipCustomerRepository.findByVipUrl(convertedUrl);
            if (customer.isPresent()) {
                System.out.println("通过 svip.html -> vip.html 转换找到客户");
                return customer;
            }
        }
        
        // 如果还是找不到，尝试将 vip.html 转换为 svip.html
        if (vipUrl.contains("vip.html")) {
            String convertedUrl = vipUrl.replace("vip.html", "svip.html");
            System.out.println("转换后的URL: " + convertedUrl);
            
            customer = vipCustomerRepository.findByVipUrl(convertedUrl);
            if (customer.isPresent()) {
                System.out.println("通过 vip.html -> svip.html 转换找到客户");
                return customer;
            }
        }
        
        System.out.println("未找到匹配的客户");
        return Optional.empty();
    }
}
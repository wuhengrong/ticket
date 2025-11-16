package com.grace.ticket.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            if (card.getStatus() != VipCard.CardStatus.AVAILABLE && card.getStatus() != VipCard.CardStatus.RESERVED) {
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
            
            vipCardRepository.save(card);
            
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
                estimatedTime
            );
            record.setEstimatedAlightingTime(estimatedTime);
            vipRecordRepository.save(record);
            
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
            vipCardRepository.save(card);
            
            // 更新使用记录
            VipRecord record = activeRecords.get(0);
            record.setAlightingStation(alightingStation);
            record.setAlightingTime(DateTimeUtils.now());
            vipRecordRepository.save(record);
            
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
        return vipCustomerRepository.findByVipUrl(vipUrl);
    }
}
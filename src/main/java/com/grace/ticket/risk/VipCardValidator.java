package com.grace.ticket.risk;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grace.ticket.dto.TicketInfoDTO;
import com.grace.ticket.entity.VipCard;

@Component
public class VipCardValidator {

    private static final String AMAP_API_KEY = "dbf17a822ded817a149e45cb535f953a";
    private static final String AMAP_BASE_URL = "https://restapi.amap.com/v3";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private VipTripValidator vipTripValidator;
    /**
     * 查找最佳匹配票卡
     */
    public Optional<VipCard> findBestMatchCard(List<VipCard> availableCards, 
                                              String boardingStation, 
                                              String alightingStation, 
                                              LocalDateTime boardingTime) {
        
        // 过滤有效票卡
        List<VipCard> validCards = availableCards.stream()
                .filter(card -> isValidCard(card, boardingTime))
                .toList();
        
       
        if (validCards.isEmpty()) {
            return Optional.empty();
        }
        
        List<TicketInfoDTO> validatedTickets = new ArrayList<>();
        
      //如果ExpiryTime是空，则为新开卡，直接
    	if(validCards.get(0).getExpiryTime()==null || validCards.get(0).getFirstUseTime()==null) {
    		return Optional.of(validCards.get(0)); 
    	}
    	
        for (VipCard card : validCards) {
        	TicketInfoDTO ticket = new TicketInfoDTO();
        	ticket.setAlightingStation(card.getAlightingStation());
        	ticket.setAlightingTime(card.getAlightingTime());
        	ticket.setTicketNumber(card.getCardNumber());
        	ticket.setBoardingStation(card.getBoardingStation());
        	ticket.setBoardingTime(card.getBoardingTime());
        	validatedTickets.add(ticket); 
        }
        
        //获取到第一个符合的Ticket就返回，减少调用高德 api次数
        List<TicketInfoDTO> tickets = vipTripValidator.validateTicketTripsForVIP(boardingStation, boardingTime, validatedTickets);
        
     // 如果tickets为空，直接返回空
        if (tickets == null || tickets.isEmpty()) {
            return Optional.empty();
        }
        
        TicketInfoDTO ticket =pickTicket(tickets);
        
        for(VipCard card:validCards ) {
        	if(card.getCardNumber()== ticket.getTicketNumber()) {
        		return Optional.of(card); 
        	}
        }
        
        // 简单选择第一个可用票卡（可根据业务逻辑优化）
        return Optional.of(validCards.get(0)); 
    }
    
    /**
     * 挑选ticket的业务逻辑，选择时间最接近的dto
     * @param tickets
     * @return
     */
    private TicketInfoDTO pickTicket(List<TicketInfoDTO> tickets) {
    	 for(TicketInfoDTO ticketDTO:tickets) {
         	ticketDTO.getSubwayTravelTime();
         }
    	return tickets.get(0);
    }

    /**
     * 计算预估出站时间
     */
    public LocalDateTime calculateEstimatedAlightingTime(String boardingStation, 
                                                        String alightingStation, 
                                                        LocalDateTime boardingTime) {
        try {
            // 调用高德API计算行程时间
            TravelTimeResult travelTime = calculateTravelTime(boardingStation, alightingStation);
            
            // 获取地铁行程时间（分钟）
            int subwayTime = travelTime.getSubwayTravelTime();
            
            // 计算预估出站时间
            return boardingTime.plusMinutes(subwayTime);
            
        } catch (Exception e) {
            // 降级方案：使用固定时间估算
            System.err.println("计算预估出站时间失败，使用降级方案: " + e.getMessage());
            return boardingTime.plusMinutes(60); // 默认1小时
        }
    }

    /**
     * 验证票卡是否有效
     */
    private boolean isValidCard(VipCard card, LocalDateTime boardingTime) {
        // 检查票卡状态
        if (card.getStatus() != VipCard.CardStatus.AVAILABLE) {
            return false;
        }
        
        // 检查失效时间
        if (card.getExpiryTime() != null && boardingTime.isAfter(card.getExpiryTime())) {
            return false;
        }
        
        return true;
    }

    /**
     * 调用高德API计算行程时间
     */
    private TravelTimeResult calculateTravelTime(String fromStation, String toStation) {
        // 复用 MetroTripAllValidator 中的逻辑
        // 这里简化为模拟实现
        
        try {
            // 模拟API调用
            Thread.sleep(500);
            
            // 简化的距离估算
            int estimatedTime = estimateSubwayTime(fromStation, toStation);
            
            TravelTimeResult result = new TravelTimeResult();
            result.setSubwayTravelTime(estimatedTime);
            return result;
            
        } catch (Exception e) {
            System.err.println("计算行程时间失败: " + e.getMessage());
            return getFallbackTravelTime(fromStation, toStation);
        }
    }

    /**
     * 估算地铁时间
     */
    private int estimateSubwayTime(String fromStation, String toStation) { 
        // 基于站点名称的简单估算
        // 实际应该根据站点数据库计算
        int baseTime = 45;
        RouteResult route;
		try {
			 String fromLocation = vipTripValidator.getStationLocation(fromStation);
	            String toLocation = vipTripValidator.getStationLocation(toStation);
			route = vipTripValidator.getSubwayRoute(fromLocation, toLocation);
			return route.getDuration(); // 最多2小时
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // 简单逻辑：根据站点名称长度估算
        
        return baseTime; // 最多2小时
    }

    /**
     * 降级方案计算行程时间
     */
    private TravelTimeResult getFallbackTravelTime(String fromStation, String toStation) {
        TravelTimeResult result = new TravelTimeResult();
        result.setSubwayTravelTime(45); // 默认45分钟
        return result;
    }

    // 内部辅助类
    public static class TravelTimeResult {
        private int subwayTravelTime;
        
        public int getSubwayTravelTime() { return subwayTravelTime; }
        public void setSubwayTravelTime(int subwayTravelTime) { this.subwayTravelTime = subwayTravelTime; }
    }
}
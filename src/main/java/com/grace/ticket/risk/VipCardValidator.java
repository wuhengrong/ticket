package com.grace.ticket.risk;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
	public Optional<VipCard> findBestMatchCard(List<VipCard> availableCards, String boardingStation,
			String alightingStation, LocalDateTime boardingTime) {

		// 过滤有效票卡
		List<VipCard> validCards = availableCards.stream().filter(card -> isValidCard(card, boardingTime)).toList();

		if (validCards.isEmpty()) {
			return Optional.empty();
		}

		// 分类票卡
		List<TicketInfoDTO> usedTickets = new ArrayList<>();
		List<TicketInfoDTO> newTickets = new ArrayList<>();
		classifyTickets(validCards, usedTickets, newTickets);

		return findBestMatchingCard(validCards, boardingStation, boardingTime, usedTickets, newTickets);
	}

	/**
	 * 将有效票卡分类为已使用和未使用的票
	 */
	private void classifyTickets(List<VipCard> validCards, List<TicketInfoDTO> usedTickets,
			List<TicketInfoDTO> newTickets) {
		for (VipCard card : validCards) {
			TicketInfoDTO ticket = createTicketInfoDTO(card);

			if (isUsedCard(card)) {
				usedTickets.add(ticket);
			} else {
				newTickets.add(ticket);
			}
		}
	}

	/**
	 * 创建TicketInfoDTO对象
	 */
	private TicketInfoDTO createTicketInfoDTO(VipCard card) {
		TicketInfoDTO ticket = new TicketInfoDTO();
		ticket.setAlightingStation(card.getAlightingStation());
		ticket.setAlightingTime(card.getAlightingTime());
		ticket.setTicketNumber(card.getCardNumber());
		ticket.setBoardingStation(card.getBoardingStation());
		ticket.setBoardingTime(card.getBoardingTime());
		return ticket;
	}

	/**
	 * 判断是否为已使用的票卡
	 */
	private boolean isUsedCard(VipCard card) {
		return card.getAlightingStation() != null && card.getAlightingTime() != null;
	}

	/**
	 * 查找最佳匹配的票卡
	 */
	private Optional<VipCard> findBestMatchingCard(List<VipCard> validCards, String boardingStation,
			LocalDateTime boardingTime, List<TicketInfoDTO> usedTickets, List<TicketInfoDTO> newTickets) {

		// 优先处理已使用的票卡
		if (!usedTickets.isEmpty()) {
			Optional<VipCard> usedCardResult = findMatchingUsedCard(validCards, boardingStation, boardingTime,
					usedTickets);
			if (usedCardResult.isPresent()) {
				return usedCardResult;
			}
		}

		// 如果没有匹配的已使用票卡，尝试使用新票卡
		return findMatchingNewCard(validCards, newTickets);
	}

	/**
	 * 查找匹配的已使用票卡
	 */
	private Optional<VipCard> findMatchingUsedCard(List<VipCard> validCards, String boardingStation,
			LocalDateTime boardingTime, List<TicketInfoDTO> usedTickets) {

		List<TicketInfoDTO> validatedTickets = vipTripValidator.validateTicketTripsForVIP(boardingStation, boardingTime,
				usedTickets);

		if (validatedTickets != null && !validatedTickets.isEmpty()) {
			TicketInfoDTO selectedTicket = validatedTickets.get(0);
			return findCardByNumber(validCards, selectedTicket.getTicketNumber());
		}

		return Optional.empty();
	}

	/**
	 * 查找匹配的新票卡
	 */
	private Optional<VipCard> findMatchingNewCard(List<VipCard> validCards, List<TicketInfoDTO> newTickets) {
		if (newTickets != null && !newTickets.isEmpty()) {
			TicketInfoDTO firstNewTicket = newTickets.get(0);
			return findCardByNumber(validCards, firstNewTicket.getTicketNumber());
		}

		return Optional.empty();
	}

	/**
	 * 根据票号查找对应的票卡
	 */
	private Optional<VipCard> findCardByNumber(List<VipCard> validCards, String ticketNumber) {
		return validCards.stream().filter(card -> Objects.equals(card.getCardNumber(), ticketNumber)).findFirst();
	}

	

	/**
	 * 计算预估出站时间
	 */
	public LocalDateTime calculateEstimatedAlightingTime(String boardingStation, String alightingStation,
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

		public int getSubwayTravelTime() {
			return subwayTravelTime;
		}

		public void setSubwayTravelTime(int subwayTravelTime) {
			this.subwayTravelTime = subwayTravelTime;
		}
	}
}
package com.grace.ticket.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grace.ticket.dto.TicketSearchRequest;
import com.grace.ticket.dto.TicketSearchResponse;
import com.grace.ticket.dto.UseTicketRequest;
import com.grace.ticket.dto.VipCardDTO;
import com.grace.ticket.entity.VipCard;
import com.grace.ticket.entity.VipCustomer;
import com.grace.ticket.entity.VipRecord;
import com.grace.ticket.repository.VipCardRepository;
import com.grace.ticket.repository.VipCustomerRepository;
import com.grace.ticket.repository.VipRecordRepository;
import com.grace.ticket.service.VipCardService;
import com.grace.ticket.util.DateTimeUtils;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/vip")
@CrossOrigin(origins = "*")
public class VipTicketController {
    
    @Autowired
    private VipCardService vipCardService;
    
    @Autowired
    private VipCardRepository vipCardRepository;

    @Autowired
    private VipCustomerRepository vipCustomerRepository;
    
    @Autowired
    private VipRecordRepository vipRecordRepository;
    
    /**
     * VIP客户登录页面
     */
    @GetMapping("/customer/{vipUrl}")
    public ResponseEntity<?> getCustomerInfo(@PathVariable String vipUrl) {
        try {
            Optional<VipCustomer> customerOpt = vipCardService.getCustomerByVipUrl(vipUrl);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("VIP客户不存在");
            }
            
            VipCustomer customer = customerOpt.get();
            List<VipRecord> history = vipCardService.getCustomerHistory(customer.getId());
            
            // 新增：检查客户是否有进行中的票务
            Map<String, Object> activeTicketInfo = getActiveTicketInfo(customer.getId()); 
            
            Map<String, Object> response = new HashMap<>();
            response.put("customer", customer);
            response.put("history", history);
            
            if((boolean)activeTicketInfo.get("hasActiveTicket")) {
            	response.put("activeTicket", activeTicketInfo); // 新增：返回进行中的票务信息
            }
             
            
            
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取客户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取客户当前进行中的票务信息
     */
    /**
     * 获取客户当前进行中的票务信息
     */
    private Map<String, Object> getActiveTicketInfo(Long customerId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找客户当前进行中的使用记录
            List<VipRecord> activeRecords = vipRecordRepository.findByVipCustomerIdAndAlightingTimeIsNull(customerId);
            
            if (activeRecords.isEmpty()) {
                result.put("hasActiveTicket", false);
                result.put("message", "没有进行中的票务");
                return result;
            }
            
            // 获取最新的进行中记录
            VipRecord activeRecord = activeRecords.get(0);
            Optional<VipCard> cardOpt = vipCardRepository.findById(activeRecord.getVipCardId());
            
            if (cardOpt.isEmpty()) {
                result.put("hasActiveTicket", false);
                result.put("message", "关联的票卡不存在");
                return result;
            }
            
            VipCard activeCard = cardOpt.get();
            
            // 构建进行中票务的详细信息 - 确保包含完整的卡号和密码
            result.put("hasActiveTicket", true);
            result.put("cardId", activeCard.getId());
            result.put("cardNumber", activeCard.getCardNumber()); // 完整卡号
            result.put("cardPassword", activeCard.getCardPassword()); // 完整密码
            result.put("boardingStation", activeRecord.getBoardingStation());
            result.put("alightingStation", activeRecord.getAlightingStation());
            result.put("boardingTime", activeRecord.getBoardingTime());
            result.put("estimatedAlightingTime", activeCard.getEstimatedAlightingTime());
            result.put("recordId", activeRecord.getId());
            
        } catch (Exception e) {
            result.put("hasActiveTicket", false);
            result.put("message", "获取进行中票务信息失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取客户当前进行中的票务（单独接口，供前端直接调用）
     */
    @GetMapping("/customer/{customerId}/active-ticket")
    public ResponseEntity<Map<String, Object>> getActiveTicket(@PathVariable Long customerId) {
        try {
            Map<String, Object> activeTicketInfo = getActiveTicketInfo(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeTicket", activeTicketInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取进行中票务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 搜索最佳匹配票卡
     */
    @PostMapping("/tickets/search")
    public ResponseEntity<TicketSearchResponse> searchTickets(@RequestBody TicketSearchRequest request) {
        TicketSearchResponse response = vipCardService.searchBestMatchCard(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 使用票卡
     */
    @PostMapping("/tickets/use")
    public ResponseEntity<TicketSearchResponse> useTicket(@RequestBody UseTicketRequest request) {
        try {
            // 验证请求参数
            if (request.getVipCustomerId() == null || request.getVipCardId() == null ||
                request.getBoardingStation() == null || request.getAlightingStation() == null) {
                return ResponseEntity.badRequest().body(TicketSearchResponse.failure("请求参数不完整"));
            }
            
            TicketSearchResponse response = vipCardService.useTicket(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(TicketSearchResponse.failure("使用票卡失败: " + e.getMessage()));
        }
    }
    
    /**
     * 归还票卡
     */
    @PostMapping("/tickets/return/{cardId}")
    @Transactional
    public ResponseEntity<TicketSearchResponse> returnTicket(
            @PathVariable Long cardId,
            @RequestParam String alightingStation,
            @RequestParam Long customerId) {
        try {
            System.out.println("收到归还票卡请求 - cardId: " + cardId + ", customerId: " + customerId + ", alightingStation: " + alightingStation);
            
            if (alightingStation == null || alightingStation.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(TicketSearchResponse.failure("出站站点不能为空"));
            }
            
            // 验证票卡和客户
            Optional<VipCard> cardOpt = vipCardRepository.findById(cardId);
            Optional<VipCustomer> customerOpt = vipCustomerRepository.findById(customerId);
            
            if (cardOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(TicketSearchResponse.failure("票卡不存在"));
            }
            if (customerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(TicketSearchResponse.failure("客户不存在"));
            }
            
            VipCard card = cardOpt.get();
            VipCustomer customer = customerOpt.get();
            
            if (card.getStatus() != VipCard.CardStatus.IN_USE) {
                return ResponseEntity.badRequest().body(TicketSearchResponse.failure("票卡未在使用中"));
            }
            
            // 查找使用记录
            List<VipRecord> activeRecords = vipRecordRepository.findActiveRecordsByCardAndCustomer(cardId, customerId);
            
            if (activeRecords.isEmpty()) {
                return ResponseEntity.badRequest().body(TicketSearchResponse.failure("未找到该客户使用此票卡的记录"));
            }
            
            // 更新票卡状态,判断是否预约客户，恢复预约状态
            if(card.getReservedUser()!=null && card.getReservedUser().contains(customer.getUserName())) {
            	card.setStatus(VipCard.CardStatus.RESERVED);
            } else {
            	card.setStatus(VipCard.CardStatus.AVAILABLE);
            }
           
            card.setInOutStatus(VipCard.InOutStatus.OUT);
            card.setAlightingStation(alightingStation);
            card.setAlightingTime(DateTimeUtils.now()); 
            card.setEstimatedAlightingTime(null);
            vipCardRepository.save(card);
            
            // 更新使用记录
            VipRecord record = activeRecords.get(0);
            record.setAlightingStation(alightingStation);
            record.setAlightingTime(DateTimeUtils.now());
            vipRecordRepository.save(record);
            
            // 构建成功响应
            VipCardDTO cardDTO = new VipCardDTO(card);
            TicketSearchResponse response = TicketSearchResponse.success(cardDTO, null, null);
            
            System.out.println("归还票卡成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("归还票卡异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(TicketSearchResponse.failure("归还票卡失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取乘车历史
     */
    @GetMapping("/history/{customerId}")
    public ResponseEntity<List<VipRecord>> getHistory(@PathVariable Long customerId) {
        List<VipRecord> history = vipCardService.getCustomerHistory(customerId);
        return ResponseEntity.ok(history);
    }
}
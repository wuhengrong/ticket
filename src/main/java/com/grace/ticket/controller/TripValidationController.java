package com.grace.ticket.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grace.ticket.dto.TicketInfoDTO;
import com.grace.ticket.entity.TicketInfo;
import com.grace.ticket.repository.TicketInfoRepository;
import com.grace.ticket.risk.MetroTripAllValidator;
import com.grace.ticket.risk.RiskAssessment;
import com.grace.ticket.risk.TripRecord;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TripValidationController {
    
    private final MetroTripAllValidator validator;
    private final TicketInfoRepository ticketInfoRepository;
    
    public TripValidationController(MetroTripAllValidator validator, TicketInfoRepository ticketInfoRepository) {
        this.validator = validator;
        this.ticketInfoRepository = ticketInfoRepository;
    }

    /**
     * 获取所有票卡信息
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketInfo>> getAllTickets() {
        try {
            List<TicketInfo> tickets = ticketInfoRepository.findAll();
            System.out.println("返回票卡数据，数量: " + tickets.size());
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            System.err.println("获取票卡数据失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 验证票卡行程 - 主要业务逻辑
     */
    @PostMapping("/validate-tickets")
    public ResponseEntity<TicketValidationResponse> validateTickets(@RequestBody TicketValidationRequest request) {
        try {
            System.out.println("收到票卡验证请求:");
            System.out.println("上车站点: " + request.getBoardingStation());
            System.out.println("上车时间: " + request.getBoardingTime());
            System.out.println("票卡数量: " + (request.getTicketInfos() != null ? request.getTicketInfos().size() : 0));
            
            if (request.getTicketInfos() == null || request.getTicketInfos().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new TicketValidationResponse(false, "票卡信息不能为空", null)
                );
            }
            
            if (request.getBoardingStation() == null || request.getBoardingStation().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new TicketValidationResponse(false, "上车站点不能为空", null)
                );
            }
            
            if (request.getBoardingTime() == null) {
                return ResponseEntity.badRequest().body(
                    new TicketValidationResponse(false, "上车时间不能为空", null)
                );
            }
            
            // 调用验证器进行行程计算
            List<TicketInfoDTO> validatedTickets = validator.validateTicketTrips(
                request.getBoardingStation(),
                request.getBoardingTime(),
                request.getTicketInfos()
            );
            
            TicketValidationResponse response = new TicketValidationResponse(
                true, 
                "验证成功", 
                validatedTickets
            );
            
            System.out.println("验证完成，返回结果数量: " + validatedTickets.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("验证票卡行程失败: " + e.getMessage());
            e.printStackTrace();
            
            TicketValidationResponse response = new TicketValidationResponse(
                false, 
                "验证失败: " + e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取可用站点列表
     */
    @GetMapping("/stations")
    public ResponseEntity<List<String>> getAvailableStations() {
        try {
            List<String> stations = validator.getAvailableStations();
            System.out.println("返回站点列表，数量: " + stations.size());
            return ResponseEntity.ok(stations);
        } catch (Exception e) {
            System.err.println("获取站点列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * 原有行程验证接口（保持兼容）
     */
    @PostMapping("/validate-trip")
    public ResponseEntity<RiskAssessmentResponse> validateTrip(@RequestBody TripValidationRequest request) {
        try {
            // 创建行程记录
            TripRecord previousTrip = new TripRecord(
                null, 
                null, 
                request.getPreviousEndStation(), 
                request.getPreviousEndTime()
            );
            
            TripRecord nextTrip = new TripRecord(
                request.getNextStartStation(),
                request.getNextStartTime(), 
                null, 
                null
            );
            
            RiskAssessment result = validator.validateTripFeasibility(previousTrip, nextTrip);
            
            RiskAssessmentResponse response = new RiskAssessmentResponse(
                result.getRiskLevel().name(),
                result.getReason(),
                result.isAllowTravel(),
                result.getConfidence()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RiskAssessmentResponse("ERROR", "评估过程中出现错误: " + e.getMessage(), false, 0.0));
        }
    }
    
    // 请求和响应 DTO
    public static class TicketValidationRequest {
        private String boardingStation;
        private LocalDateTime boardingTime;
        private List<TicketInfoDTO> ticketInfos;
        
        public String getBoardingStation() { return boardingStation; }
        public void setBoardingStation(String boardingStation) { this.boardingStation = boardingStation; }
        public LocalDateTime getBoardingTime() { return boardingTime; }
        public void setBoardingTime(LocalDateTime boardingTime) { this.boardingTime = boardingTime; }
        public List<TicketInfoDTO> getTicketInfos() { return ticketInfos; }
        public void setTicketInfos(List<TicketInfoDTO> ticketInfos) { this.ticketInfos = ticketInfos; }
    }

    public static class TicketValidationResponse {
        private boolean success;
        private String message;
        private List<TicketInfoDTO> validatedTickets;
        
        public TicketValidationResponse() {}
        
        public TicketValidationResponse(boolean success, String message, List<TicketInfoDTO> validatedTickets) {
            this.success = success;
            this.message = message;
            this.validatedTickets = validatedTickets;
        }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<TicketInfoDTO> getValidatedTickets() { return validatedTickets; }
        public void setValidatedTickets(List<TicketInfoDTO> validatedTickets) { this.validatedTickets = validatedTickets; }
    }

    public static class TripValidationRequest {
        private String previousEndStation;
        private LocalDateTime previousEndTime;
        private String nextStartStation;
        private LocalDateTime nextStartTime;
        
        public String getPreviousEndStation() { return previousEndStation; }
        public void setPreviousEndStation(String previousEndStation) { this.previousEndStation = previousEndStation; }
        public LocalDateTime getPreviousEndTime() { return previousEndTime; }
        public void setPreviousEndTime(LocalDateTime previousEndTime) { this.previousEndTime = previousEndTime; }
        public String getNextStartStation() { return nextStartStation; }
        public void setNextStartStation(String nextStartStation) { this.nextStartStation = nextStartStation; }
        public LocalDateTime getNextStartTime() { return nextStartTime; }
        public void setNextStartTime(LocalDateTime nextStartTime) { this.nextStartTime = nextStartTime; }
    }
    
    public static class RiskAssessmentResponse {
        private String riskLevel;
        private String reason;
        private boolean allowTravel;
        private double confidence;
        
        public RiskAssessmentResponse(String riskLevel, String reason, boolean allowTravel, double confidence) {
            this.riskLevel = riskLevel;
            this.reason = reason;
            this.allowTravel = allowTravel;
            this.confidence = confidence;
        }
        
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public boolean isAllowTravel() { return allowTravel; }
        public void setAllowTravel(boolean allowTravel) { this.allowTravel = allowTravel; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
}
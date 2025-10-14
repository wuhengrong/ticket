package com.grace.ticket.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grace.ticket.risk.MetroTripAllValidator;
import com.grace.ticket.risk.RiskAssessment;
import com.grace.ticket.risk.TripRecord;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TripValidationController {
    
    private final MetroTripAllValidator validator;
    
    public TripValidationController(MetroTripAllValidator validator) {
        this.validator = validator;
    }
    
 // 处理站点名称的方法
    private String processStationName(String stationName) {
        if (stationName == null || stationName.trim().isEmpty()) {
            return stationName;
        }
        
        String processedName = stationName.trim();
        
        // 如果已经包含"地铁"或"地铁站"，直接返回
        if (processedName.contains("地铁") || processedName.contains("地铁站")) {
            return processedName;
        }
        
        // 否则在末尾添加"地铁站"
        return processedName + "地铁站";
    }
    
    @PostMapping("/validate-trip")
    public ResponseEntity<RiskAssessmentResponse> validateTrip(@RequestBody TripValidationRequest request) {
        try {
            // 解析时间
            //LocalDateTime previousEndTime = LocalDateTime.parse(request.getPreviousEndTime());
            //LocalDateTime nextStartTime = LocalDateTime.parse(request.getNextStartTime());
            
            // 方案1: 使用 Instant 解析带时区的时间，然后转换为 LocalDateTime
            Instant previousEndInstant = Instant.parse(request.getPreviousEndTime());
            Instant nextStartInstant = Instant.parse(request.getNextStartTime());
            
            // 转换为系统默认时区的 LocalDateTime
            LocalDateTime previousEndTime = previousEndInstant.atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime nextStartTime = nextStartInstant.atZone(ZoneId.systemDefault()).toLocalDateTime();
            
            // 创建行程记录
            TripRecord previousTrip = new TripRecord(
                null, 
                null, 
                processStationName(request.getPreviousEndStation()), 
                previousEndTime
            );
            
            TripRecord nextTrip = new TripRecord(
            	processStationName(request.getNextStartStation()),
                nextStartTime, 
                null, 
                null
            );
            
            // 调用验证器
            RiskAssessment result = validator.validateTripFeasibility(previousTrip, nextTrip);
            
            // 转换为响应对象
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
    public static class TripValidationRequest {
        private String previousEndStation;
        private String previousEndTime;
        private String nextStartStation;
        private String nextStartTime;
        
        // getters and setters
        public String getPreviousEndStation() { return previousEndStation; }
        public void setPreviousEndStation(String previousEndStation) { this.previousEndStation = previousEndStation; }
        public String getPreviousEndTime() { return previousEndTime; }
        public void setPreviousEndTime(String previousEndTime) { this.previousEndTime = previousEndTime; }
        public String getNextStartStation() { return nextStartStation; }
        public void setNextStartStation(String nextStartStation) { this.nextStartStation = nextStartStation; }
        public String getNextStartTime() { return nextStartTime; }
        public void setNextStartTime(String nextStartTime) { this.nextStartTime = nextStartTime; }
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
        
        // getters and setters
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
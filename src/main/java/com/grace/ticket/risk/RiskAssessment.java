package com.grace.ticket.risk;

//风险评估结果类
public class RiskAssessment {
 public enum RiskLevel {
     NORMAL,         // 正常
     SUSPICIOUS,     // 可疑
     HIGH_RISK,      // 高风险
     IMPOSSIBLE      // 物理不可能
 }
 
 private RiskLevel riskLevel;
 private String reason;
 private boolean allowTravel;
 private double confidence; // 置信度 0-1
 
 public RiskAssessment(RiskLevel riskLevel, String reason, boolean allowTravel, double confidence) {
     this.riskLevel = riskLevel;
     this.reason = reason;
     this.allowTravel = allowTravel;
     this.confidence = confidence;
 }
 
 // getters
 public RiskLevel getRiskLevel() { return riskLevel; }
 public String getReason() { return reason; }
 public boolean isAllowTravel() { return allowTravel; }
 public double getConfidence() { return confidence; }
}

package com.grace.ticket.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ticket_cards")
public class TicketCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "serial_number")
    private Integer serialNumber;
    
    @Column(name = "card_url")  // 改为单个URL字段
    private String cardUrl;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Column(name = "status")
    private String status = "可用";
    
    @Column(name = "user_url")
    private String userUrl;
    
    @Column(name = "generated_code")
    private String generatedCode;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();
    
    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getSerialNumber() { return serialNumber; }
    public void setSerialNumber(Integer serialNumber) { this.serialNumber = serialNumber; }
    
    public String getCardUrl() { return cardUrl; }  // 改为getCardUrl
    public void setCardUrl(String cardUrl) { this.cardUrl = cardUrl; }  // 改为setCardUrl
    
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getUserUrl() { return userUrl; }
    public void setUserUrl(String userUrl) { this.userUrl = userUrl; }
    
    public String getGeneratedCode() { return generatedCode; }
    public void setGeneratedCode(String generatedCode) { this.generatedCode = generatedCode; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
package com.grace.ticket.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.grace.ticket.dto.CardCredentials;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "virtual_cards")
@NoArgsConstructor
@AllArgsConstructor
public class VirtualCard {
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNotAvailableDates() {
		return notAvailableDates;
	}

	public void setNotAvailableDates(String notAvailableDates) {
		this.notAvailableDates = notAvailableDates;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getPeriodStatus() {
		return periodStatus;
	}

	public void setPeriodStatus(Integer periodStatus) {
		this.periodStatus = periodStatus;
	}

	public String getPhysicalCardAPhone() {
		return physicalCardAPhone;
	}

	public void setPhysicalCardAPhone(String physicalCardAPhone) {
		this.physicalCardAPhone = physicalCardAPhone;
	}

	public String getPhysicalCardAPwd() {
		return physicalCardAPwd;
	}

	public void setPhysicalCardAPwd(String physicalCardAPwd) {
		this.physicalCardAPwd = physicalCardAPwd;
	}

	public String getPhysicalCardBPhone() {
		return physicalCardBPhone;
	}

	public void setPhysicalCardBPhone(String physicalCardBPhone) {
		this.physicalCardBPhone = physicalCardBPhone;
	}

	public String getPhysicalCardBPwd() {
		return physicalCardBPwd;
	}

	public void setPhysicalCardBPwd(String physicalCardBPwd) {
		this.physicalCardBPwd = physicalCardBPwd;
	}

	public String getUsageRule() {
		return usageRule;
	}

	public void setUsageRule(String usageRule) {
		this.usageRule = usageRule;
	}

	public String getCurrentUsedBy() {
		return currentUsedBy;
	}

	public LocalDateTime getCurrentUsageStartTime() {
		return currentUsageStartTime;
	}

	public Integer getCurrentStatus() {
		return currentStatus;
	}

	@Id
    private String id;
    
    @Column(name = "not_available_dates")
    private String notAvailableDates;
    
    private Integer status;
    
    @Column(name = "period_status")
    private Integer periodStatus =1;
    
    @Column(name = "physical_card_a_phone")
    private String physicalCardAPhone;
    
    @Column(name = "physical_card_a_pwd")
    private String physicalCardAPwd;
    
    @Column(name = "physical_card_b_phone")
    private String physicalCardBPhone;
    
    @Column(name = "physical_card_b_pwd")
    private String physicalCardBPwd;
    
    @Column(name = "usage_rule")
    private String usageRule;
    
    @Column(name = "current_used_by")
    private String currentUsedBy;
    
    @Column(name = "current_usage_start_time")
    private LocalDateTime currentUsageStartTime;
    
    @Column(name = "current_status")
    private Integer currentStatus = 1;//原来为0，修改为1
    
    @Column(name = "card_initial_start_time")
    private LocalDateTime cardInitialStartTime;
    
    @Column(name = "card_name", length = 100)
    private String cardName;
    
    // getter 和 setter
    public String getCardName() {
        return cardName;
    }
    
    public void setCardName(String cardName) {
        this.cardName = cardName;
    }
    
    // 手动添加setter方法（如果Lombok不工作）
    public void setCurrentUsedBy(String currentUsedBy) {
        this.currentUsedBy = currentUsedBy;
    }
    
    public void setCurrentUsageStartTime(LocalDateTime currentUsageStartTime) {
        this.currentUsageStartTime = currentUsageStartTime;
    }
    
    public void setCurrentStatus(Integer currentStatus) {
        this.currentStatus = currentStatus;
    }
    
    // 业务方法保持不变...
    public boolean isAvailable() {
        return status == 1;
    }
    
    public boolean isPeriodAvailable() {
        return periodStatus == 1;
    }
    
    public List<LocalDate> getNotAvailableDateList() {
        if (notAvailableDates == null || notAvailableDates.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(notAvailableDates.split(";"))
                .map(dateStr -> {
                    String[] parts = dateStr.split("\\.");
                    return LocalDate.of(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2])
                    );
                })
                .collect(Collectors.toList());
    }
    
    public List<Integer> getUsageRuleDays() {
        if (usageRule == null || usageRule.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(usageRule.split(";"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
    
    public boolean isTodayAvailable() {
        LocalDate today = LocalDate.now();
        
        if (getNotAvailableDateList().contains(today)) {
            return false;
        }
        
        if (!isAvailable()) {
            return false;
        }
        
        //int todayDayOfWeek = today.getDayOfWeek().getValue();
        //return getUsageRuleDays().contains(todayDayOfWeek);
        return true;
    }
    
    public CardCredentials getCurrentCredentials() {
        LocalDate today = LocalDate.now();
        int todayDayOfWeek = today.getDayOfWeek().getValue();
        List<Integer> ruleDays = getUsageRuleDays();
        
        if (ruleDays.contains(todayDayOfWeek)) {
            return new CardCredentials(physicalCardAPhone, physicalCardAPwd);
        } else {
            return new CardCredentials(physicalCardBPhone, physicalCardBPwd);
        }
    }
    
 // 在 VirtualCard 实体类中添加重置方法
    public void resetToInitialState() {
        this.periodStatus = 1;
        this.currentUsedBy = null;
        this.currentUsageStartTime = null;
        this.currentStatus = 1; // 重置为可用状态
        this.cardInitialStartTime = null;
        
        // 如果需要重置其他字段，可以在这里添加
        // this.status = 1;
    }
    public boolean canUserUseCard(String groupId, String userId, Integer userOrder) {
        
    	//status = 1,可用
    	if (!isAvailable()) {
            return false;
        }
        
    	//检查不可用列表是否包含今天
        if (!isTodayAvailable()) {
            return false;
        }
        
        //判断用户顺序
        /*
        if (!periodStatus.equals(userOrder)) { 
            return false;
        }
        */
        
        String orderSeq = userOrder.toString();
        String curSeq = "" + periodStatus;
        if (!orderSeq.contains(curSeq)) { 
            return false;
        }
        
        
        if (currentUsedBy != null && !currentUsedBy.equals(userId)) {
            return false;
        }
        
        return true;
    }

	public LocalDateTime getCardInitialStartTime() {
		return cardInitialStartTime;
	}

	public void setCardInitialStartTime(LocalDateTime cardInitialStartTime) {
		this.cardInitialStartTime = cardInitialStartTime;
	}
}
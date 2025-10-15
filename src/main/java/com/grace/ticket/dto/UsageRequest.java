package com.grace.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageRequest {
    private String groupId;
    private String userId;
    private String accessCode; // 新增字段
    
    public String getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}

	// 如果Lombok不工作，手动添加getter方法
    public String getGroupId() {
        return groupId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    // setter方法
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
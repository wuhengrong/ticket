package com.grace.ticket.dto;

import lombok.Data;

// UsageResponse.java - 手动实现Builder
@Data
public class UsageResponse {
    private boolean success;
    private String message;
    private Object data;
    
    // 私有构造函数用于Builder
    private UsageResponse(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.data = builder.data;
    }
    
    public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	// 手动实现Builder类
    public static class Builder {
        private boolean success;
        private String message;
        private Object data;
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder data(Object data) {
            this.data = data;
            return this;
        }
        
        public UsageResponse build() {
            return new UsageResponse(this);
        }
    }
    
    // 静态builder方法
    public static Builder builder() {
        return new Builder();
    }
    
    public static UsageResponse success(String message, Object data) {
        return UsageResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    public static UsageResponse failed(String message) {
        return UsageResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
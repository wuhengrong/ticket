package com.grace.ticket.dto;

import lombok.Data;

@Data
public class CardInfo {
    private String virtualCardId;
    private String phone;
    public String getVirtualCardId() {
		return virtualCardId;
	}

	public void setVirtualCardId(String virtualCardId) {
		this.virtualCardId = virtualCardId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(Integer currentStatus) {
		this.currentStatus = currentStatus;
	}

	public String getCurrentUsedBy() {
		return currentUsedBy;
	}

	public void setCurrentUsedBy(String currentUsedBy) {
		this.currentUsedBy = currentUsedBy;
	}

	public Boolean getTodayAvailable() {
		return todayAvailable;
	}

	public void setTodayAvailable(Boolean todayAvailable) {
		this.todayAvailable = todayAvailable;
	}

	public Boolean getPeriodAvailable() {
		return periodAvailable;
	}

	public void setPeriodAvailable(Boolean periodAvailable) {
		this.periodAvailable = periodAvailable;
	}

	private String password;
    private Integer status;
    private Integer currentStatus;
    private String currentUsedBy;
    private Boolean todayAvailable;
    private Boolean periodAvailable;

    // 私有构造函数用于Builder
    private CardInfo(Builder builder) {
        this.virtualCardId = builder.virtualCardId;
        this.phone = builder.phone;
        this.password = builder.password;
        this.status = builder.status;
        this.currentStatus = builder.currentStatus;
        this.currentUsedBy = builder.currentUsedBy;
        this.todayAvailable = builder.todayAvailable;
        this.periodAvailable = builder.periodAvailable;
    }

    // 手动实现Builder类
    public static class Builder {
        private String virtualCardId;
        private String phone;
        private String password;
        private Integer status;
        private Integer currentStatus;
        private String currentUsedBy;
        private Boolean todayAvailable;
        private Boolean periodAvailable;

        public Builder virtualCardId(String virtualCardId) {
            this.virtualCardId = virtualCardId;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder currentStatus(Integer currentStatus) {
            this.currentStatus = currentStatus;
            return this;
        }

        public Builder currentUsedBy(String currentUsedBy) {
            this.currentUsedBy = currentUsedBy;
            return this;
        }

        public Builder todayAvailable(Boolean todayAvailable) {
            this.todayAvailable = todayAvailable;
            return this;
        }

        public Builder periodAvailable(Boolean periodAvailable) {
            this.periodAvailable = periodAvailable;
            return this;
        }

        public CardInfo build() {
            return new CardInfo(this);
        }
    }

    // 静态builder方法
    public static Builder builder() {
        return new Builder();
    }
}
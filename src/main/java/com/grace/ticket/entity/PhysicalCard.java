package com.grace.ticket.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PhysicalCard {
    private String id;
    private String phone;
    private String passwd;
    private String status; // ACTIVE, INACTIVE, SUSPENDED
    private LocalDateTime cardOpenTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public LocalDateTime getCardOpenTime() {
		return cardOpenTime;
	}
	public void setCardOpenTime(LocalDateTime cardOpenTime) {
		this.cardOpenTime = cardOpenTime;
	}
}
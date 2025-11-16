package com.grace.ticket.dto;

import lombok.Data;

@Data
public class GenerateRideLinkResponse {
    private boolean success;
    private String message;
    private String cardUrl;
    private Integer remainingRides;
    
    public static GenerateRideLinkResponse success(String cardUrl, Integer remainingRides) {
        GenerateRideLinkResponse response = new GenerateRideLinkResponse();
        response.setSuccess(true);
        response.setCardUrl(cardUrl);
        response.setRemainingRides(remainingRides);
        response.setMessage("二维码链接生成成功");
        return response;
    }
    
    public static GenerateRideLinkResponse error(String message) {
        GenerateRideLinkResponse response = new GenerateRideLinkResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
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

	public String getCardUrl() {
		return cardUrl;
	}

	public void setCardUrl(String cardUrl) {
		this.cardUrl = cardUrl;
	}

	public Integer getRemainingRides() {
		return remainingRides;
	}

	public void setRemainingRides(Integer remainingRides) {
		this.remainingRides = remainingRides;
	}
}
package com.grace.ticket.dto;

import java.time.LocalDateTime;

/**
 * 票卡信息数据传输对象
 */
public class TicketInfoDTO {
    private Long id;
    private String ticketNumber;
    private String boardingStation;
    private LocalDateTime boardingTime;
    private String alightingStation;
    private LocalDateTime alightingTime;
    private Integer subwayTravelTime;
    private Integer timeInterval;
    private Integer subwayWalkTime;
    
    private String greeLight;
    
    public String getGreeLight() {
		return greeLight;
	}

	public void setGreeLight(String greeLight) {
		this.greeLight = greeLight;
	}

	public Integer getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(Integer timeInterval) {
		this.timeInterval = timeInterval;
	}

	private Integer subwayWaitTime;
    private Integer taxiTime;
    private String travelSuggestion;

    // 构造函数
    public TicketInfoDTO() {}

    public TicketInfoDTO(String ticketNumber, String alightingStation, LocalDateTime alightingTime) {
        this.ticketNumber = ticketNumber;
        this.alightingStation = alightingStation;
        this.alightingTime = alightingTime;
    }

    // Getter 和 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getBoardingStation() { return boardingStation; }
    public void setBoardingStation(String boardingStation) { this.boardingStation = boardingStation; }

    public LocalDateTime getBoardingTime() { return boardingTime; }
    public void setBoardingTime(LocalDateTime boardingTime) { this.boardingTime = boardingTime; }

    public String getAlightingStation() { return alightingStation; }
    public void setAlightingStation(String alightingStation) { this.alightingStation = alightingStation; }

    public LocalDateTime getAlightingTime() { return alightingTime; }
    public void setAlightingTime(LocalDateTime alightingTime) { this.alightingTime = alightingTime; }

    public Integer getSubwayTravelTime() { return subwayTravelTime; }
    public void setSubwayTravelTime(Integer subwayTravelTime) { this.subwayTravelTime = subwayTravelTime; }

    public Integer getSubwayWalkTime() { return subwayWalkTime; }
    public void setSubwayWalkTime(Integer subwayWalkTime) { this.subwayWalkTime = subwayWalkTime; }

    public Integer getSubwayWaitTime() { return subwayWaitTime; }
    public void setSubwayWaitTime(Integer subwayWaitTime) { this.subwayWaitTime = subwayWaitTime; }

    public Integer getTaxiTime() { return taxiTime; }
    public void setTaxiTime(Integer taxiTime) { this.taxiTime = taxiTime; }

    public String getTravelSuggestion() { return travelSuggestion; }
    public void setTravelSuggestion(String travelSuggestion) { this.travelSuggestion = travelSuggestion; }

    @Override
    public String toString() {
        return "TicketInfoDTO{" +
                "id=" + id +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", boardingStation='" + boardingStation + '\'' +
                ", boardingTime=" + boardingTime +
                ", alightingStation='" + alightingStation + '\'' +
                ", alightingTime=" + alightingTime +
                ", subwayTravelTime=" + subwayTravelTime +
                ", subwayWalkTime=" + subwayWalkTime +
                ", subwayWaitTime=" + subwayWaitTime +
                ", taxiTime=" + taxiTime +
                ", travelSuggestion='" + travelSuggestion + '\'' +
                '}';
    }
}
package com.grace.ticket.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.grace.ticket.entity.TicketInfo;

//TripValidationRequest.java
public class TripValidationRequest {
 private String boardingStation;
 private LocalDateTime boardingTime;
 private List<TicketInfo> ticketInfos;
 
 // constructors, getters, setters
 public TripValidationRequest() {}
 
 public TripValidationRequest(String boardingStation, LocalDateTime boardingTime, List<TicketInfo> ticketInfos) {
     this.boardingStation = boardingStation;
     this.boardingTime = boardingTime;
     this.ticketInfos = ticketInfos;
 }

public String getBoardingStation() {
	return boardingStation;
}

public void setBoardingStation(String boardingStation) {
	this.boardingStation = boardingStation;
}

public LocalDateTime getBoardingTime() {
	return boardingTime;
}

public void setBoardingTime(LocalDateTime boardingTime) {
	this.boardingTime = boardingTime;
}

public List<TicketInfo> getTicketInfos() {
	return ticketInfos;
}

public void setTicketInfos(List<TicketInfo> ticketInfos) {
	this.ticketInfos = ticketInfos;
}
 
 // getters and setters...
}


package com.grace.ticket.dto;

import java.util.List;

import com.grace.ticket.entity.TicketInfo;

//ValidationResult.java
public class ValidationResult {
private boolean success;
private String message;
private List<TicketInfo> validatedTickets;

// constructors, getters, setters
public ValidationResult(boolean success, String message, List<TicketInfo> validatedTickets) {
   this.success = success;
   this.message = message;
   this.validatedTickets = validatedTickets;
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

public List<TicketInfo> getValidatedTickets() {
	return validatedTickets;
}

public void setValidatedTickets(List<TicketInfo> validatedTickets) {
	this.validatedTickets = validatedTickets;
}

// getters and setters...
}
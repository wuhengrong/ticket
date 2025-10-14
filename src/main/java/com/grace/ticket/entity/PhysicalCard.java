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
}
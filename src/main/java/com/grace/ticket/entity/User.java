package com.grace.ticket.entity;

import lombok.Data;

@Data
public class User {
    private String userId;
    private String username;
    private String nickname;
    private String userLevel; // SVIP, VIP, 普通
}
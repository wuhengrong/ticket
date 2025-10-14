package com.grace.ticket.dto;

public class CardCredentials {
    private String phone;
    private String password;
    
    // 无参构造函数
    public CardCredentials() {
    }
    
    // 全参构造函数
    public CardCredentials(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
    
    // Getter 和 Setter 方法
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
    
    @Override
    public String toString() {
        return "CardCredentials{" +
                "phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
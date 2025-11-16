package com.grace.ticket.dto;

import com.grace.ticket.entity.VipQrRecord;

public class GetActiveQrResponse {
    private boolean success;
    private String message;
    private VipQrRecord qrRecord;
    
    // 构造函数
    public GetActiveQrResponse() {}
    
    public GetActiveQrResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public GetActiveQrResponse(boolean success, String message, VipQrRecord qrRecord) {
        this.success = success;
        this.message = message;
        this.qrRecord = qrRecord;
    }
    
    // getters and setters
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
    
    public VipQrRecord getQrRecord() {
        return qrRecord;
    }
    
    public void setQrRecord(VipQrRecord qrRecord) {
        this.qrRecord = qrRecord;
    }
}
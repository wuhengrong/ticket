package com.grace.ticket.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

@Component
public class DateTimeUtils {
    
    public static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    
    /**
     * 获取当前北京时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(BEIJING_ZONE);
    }
    
    
    
    /**
     * 转换为北京时间
     */
    public static LocalDateTime toBeijingTime(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault())
                           .withZoneSameInstant(BEIJING_ZONE)
                           .toLocalDateTime();
    }
    
    /**
     * 转换为北京时间（从Instant）
     */
    public static LocalDateTime toBeijingTime(Instant instant) {
        return instant.atZone(BEIJING_ZONE).toLocalDateTime();
    }
    
    /**
     * 将字符串解析为北京时间
     */
    public static LocalDateTime parseBeijingTime(String timeStr) {
        return LocalDateTime.parse(timeStr).atZone(BEIJING_ZONE).toLocalDateTime();
    }
}
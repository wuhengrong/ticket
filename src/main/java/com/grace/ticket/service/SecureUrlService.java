// SecureUrlService.java
package com.grace.ticket.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SecureUrlService {
    
    // 存储访问码到用户信息的映射（可选，也可以直接从数据库查询）
    private final Map<String, String[]> accessCodeMap = new HashMap<>();
    
    /**
     * 生成唯一的访问码
     */
    public String generateAccessCode() {
        // 使用UUID生成16位的随机码（足够安全且不易猜测）
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * 简单的固定accessCode生成方法（备用）
     */
    public String generateSimpleFixedAccessCode(String userId, String groupId) {
        // 使用简单的哈希组合
        String combined = userId + groupId;
        int hash = combined.hashCode();
        
        // 确保为正数并转换为16进制
        String hex = Integer.toHexString(Math.abs(hash));
        
        // 填充到16位，不足补0，超过则截取
        if (hex.length() < 16) {
            StringBuilder sb = new StringBuilder(hex);
            while (sb.length() < 16) {
                sb.append('0');
            }
            return sb.toString().toUpperCase();
        } else {
            return hex.substring(0, 16).toUpperCase();
        }
    }
    
    /**
     * 存储访问码映射（可选）
     */
    public void storeAccessCode(String accessCode, String groupId, String userId) {
        accessCodeMap.put(accessCode, new String[]{groupId, userId});
    }
    
    /**
     * 根据访问码获取用户信息（可选）
     */
    public String[] getUserInfoByAccessCode(String accessCode) {
        return accessCodeMap.get(accessCode);
    }
    
    /**
     * 验证访问码格式
     */
    public boolean isValidAccessCode(String accessCode) {
        return accessCode != null && accessCode.matches("[a-f0-9]{16}");
    }
}
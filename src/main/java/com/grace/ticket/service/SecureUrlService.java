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
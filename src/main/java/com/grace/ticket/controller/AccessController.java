// AccessController.java
package com.grace.ticket.controller;

import com.grace.ticket.dto.ApiResponse;
import com.grace.ticket.entity.GroupMember;
import com.grace.ticket.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/access")
@CrossOrigin(origins = "*")
public class AccessController {
    
    @Autowired
    private GroupMemberService groupMemberService;
    
    /**
     * 验证访问码并返回用户信息
     */
    @GetMapping("/validate")
    public ApiResponse<GroupMember> validateAccessCode(@RequestParam String code) {
        try {
            if (code == null || code.trim().isEmpty()) {
                return ApiResponse.error("访问码不能为空");
            }
            
            // 从数据库查询对应的用户信息
            Optional<GroupMember> member = groupMemberService.findByAccessCode(code);
            if (member.isPresent()) {
                GroupMember userInfo = member.get();
                // 返回用户信息（不包含敏感信息）
                GroupMember safeInfo = new GroupMember();
                safeInfo.setGroupId(userInfo.getGroupId());
                safeInfo.setUserId(userInfo.getUserId());
                safeInfo.setUseOrder(userInfo.getUseOrder());
                // 可以返回其他需要的信息
                
                return ApiResponse.success(safeInfo);
            } else {
                return ApiResponse.error("无效的访问码");
            }
        } catch (Exception e) {
            return ApiResponse.error("验证失败: " + e.getMessage());
        }
    }
}
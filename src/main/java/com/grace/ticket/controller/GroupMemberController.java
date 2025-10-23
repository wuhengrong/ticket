// GroupMemberController.java
package com.grace.ticket.controller;

import com.grace.ticket.config.Constants;
import com.grace.ticket.dto.ApiResponse;
import com.grace.ticket.entity.GroupMember;
import com.grace.ticket.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/group-members")
public class GroupMemberController {
    
    @Autowired
    private GroupMemberService groupMemberService;
    
    // 密码校验方法
    private boolean validatePassword(String password) {
        return Constants.MANAGEMENT_ACCESS_PASSWORD.equals(password);
    }
    /*
    // 获取所有成员
    @GetMapping
    public ApiResponse<List<GroupMember>> getAllGroupMembers(@RequestBody(required = false) PasswordRequest passwordRequest) {
        try {
            // 密码校验
            if (passwordRequest == null || !validatePassword(passwordRequest.getPassword())) {
                return ApiResponse.error("未授权访问：密码错误");
            }
            
            List<GroupMember> members = groupMemberService.findAll();
            return ApiResponse.success(members);
        } catch (Exception e) {
            return ApiResponse.error("获取成员列表失败: " + e.getMessage());
        }
    }
    */
    @GetMapping
    public ApiResponse<List<GroupMember>> getAllGroupMembers(
            @RequestParam(required = false) String password) {
        try {
            // 密码校验
            if (password == null || !validatePassword(password)) {
                return ApiResponse.error("未授权访问：密码错误");
            }
            
            List<GroupMember> members = groupMemberService.findAll();
            return ApiResponse.success(members);
        } catch (Exception e) {
            return ApiResponse.error("获取成员列表失败: " + e.getMessage());
        }
    }
    
    // 根据分组ID获取成员
    @GetMapping("/group/{groupId}")
    public ApiResponse<List<GroupMember>> getMembersByGroupId(@PathVariable String groupId, @RequestBody(required = false) PasswordRequest passwordRequest) {
        try {
            // 密码校验
            if (passwordRequest == null || !validatePassword(passwordRequest.getPassword())) {
                return ApiResponse.error("未授权访问：密码错误");
            }
            
            List<GroupMember> members = groupMemberService.findByGroupId(groupId);
            return ApiResponse.success(members);
        } catch (Exception e) {
            return ApiResponse.error("获取分组成员失败: " + e.getMessage());
        }
    }
    
    // 根据分组ID和用户ID获取成员
    @GetMapping("/{groupId}/{userId}")
    public ApiResponse<GroupMember> getGroupMember(@PathVariable String groupId, @PathVariable String userId, @RequestBody(required = false) PasswordRequest passwordRequest) {
        try {
            // 密码校验
            if (passwordRequest == null || !validatePassword(passwordRequest.getPassword())) {
                return ApiResponse.error("未授权访问：密码错误");
            }
            
            Optional<GroupMember> member = groupMemberService.findByGroupIdAndUserId(groupId, userId);
            if (member.isPresent()) {
                return ApiResponse.success(member.get());
            } else {
                return ApiResponse.error("成员不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("获取成员信息失败: " + e.getMessage());
        }
    }
    
    // 创建或更新成员
    @PostMapping
    public ApiResponse<GroupMember> saveGroupMember(@RequestBody GroupMemberRequest request) {
        try {
            // 密码校验
            if (!validatePassword(request.getPassword())) {
                return ApiResponse.error("未授权访问：密码错误");
            }
            
            GroupMember groupMember = request.getGroupMember();
            
            // 检查是否已存在
            Optional<GroupMember> existingMember = groupMemberService
                .findByGroupIdAndUserId(groupMember.getGroupId(), groupMember.getUserId());
            
            if (existingMember.isPresent()) {
                // 更新操作
                groupMember.setId(existingMember.get().getId()); // 保持ID不变
            }
            
            GroupMember savedMember = groupMemberService.save(groupMember);
            String message = existingMember.isPresent() ? "成员更新成功" : "成员创建成功";
            return ApiResponse.success(savedMember, message);
        } catch (Exception e) {
            return ApiResponse.error("保存成员失败: " + e.getMessage());
        }
    }
    
    // 删除成员
    @DeleteMapping("/{groupId}/{userId}")
    public ApiResponse<Void> deleteGroupMember(@PathVariable String groupId, @PathVariable String userId, @RequestBody(required = false) PasswordRequest passwordRequest) {
        try {
            // 密码校验
            if (passwordRequest == null || !validatePassword(passwordRequest.getPassword())) {
                return ApiResponse.error("未授权访问：密码错误");
            }
            
            Optional<GroupMember> member = groupMemberService.findByGroupIdAndUserId(groupId, userId);
            if (!member.isPresent()) {
                return ApiResponse.error("成员不存在");
            }
            
            groupMemberService.deleteByGroupIdAndUserId(groupId, userId);
            return ApiResponse.success(null, "成员删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除成员失败: " + e.getMessage());
        }
    }
    
    // 密码请求DTO
    public static class PasswordRequest {
        private String password;
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    // 成员请求DTO（包含密码）
    public static class GroupMemberRequest {
        private String password;
        private GroupMember groupMember;
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public GroupMember getGroupMember() {
            return groupMember;
        }
        
        public void setGroupMember(GroupMember groupMember) {
            this.groupMember = groupMember;
        }
    }
}
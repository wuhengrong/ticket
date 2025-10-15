package com.grace.ticket.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grace.ticket.dto.ApiResponse;
import com.grace.ticket.dto.CardInfo;
import com.grace.ticket.dto.UsageRequest;
import com.grace.ticket.entity.GroupMember;
import com.grace.ticket.entity.UsageRecord;
import com.grace.ticket.service.AccessValidationService;
import com.grace.ticket.service.GroupUsageService;
import com.grace.ticket.service.VirtualCardService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GroupUsageController {
    @Autowired
    private AccessValidationService accessValidationService;
     
    @Autowired
    private GroupUsageService groupUsageService;
    
    @Autowired
    private VirtualCardService virtualCardService;
    
    @GetMapping("/group-members/{groupId}/{userId}")
    public ResponseEntity<ApiResponse<GroupMember>> getUserInfo(
            @PathVariable String groupId, 
            @PathVariable String userId,
            @RequestParam(required = false) String accessCode) {
        try {
            // 只有当提供了accessCode参数时才进行验证
            if (accessCode != null && !accessCode.isEmpty()) {
                if (!accessValidationService.validateAccess(userId, groupId, accessCode)) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("访问被拒绝：无效的访问码"));
                }
            }
            
            GroupMember member = groupUsageService.getUserInfo(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(member));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/virtual-cards/group/{groupId}")
    public ResponseEntity<ApiResponse<CardInfo>> getVirtualCardInfo(@PathVariable String groupId) {
        try {
            CardInfo cardInfo = virtualCardService.getCardInfo(groupId);
            return ResponseEntity.ok(ApiResponse.success(cardInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // 获取用户完整的虚拟卡信息（包含用户信息和虚拟卡信息）
    @GetMapping("/virtual-cards/user-info")
    public ResponseEntity<ApiResponse<Object>> getUserVirtualCardInfo(
            @RequestParam String groupId,
            @RequestParam String userId,
            @RequestParam(required = false) String accessCode) {
        try {
            // 只有当提供了accessCode参数时才进行验证
            if (accessCode != null && !accessCode.isEmpty()) {
                if (!accessValidationService.validateAccess(userId, groupId, accessCode)) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("访问被拒绝：无效的访问码"));
                }
            }
            
            // 获取用户信息
            GroupMember member = groupUsageService.getUserInfo(groupId, userId);
            // 获取虚拟卡信息
            CardInfo cardInfo = virtualCardService.getCardInfo(groupId);
            
            // 创建组合响应对象
            UserCardInfo userCardInfo = new UserCardInfo();
            userCardInfo.setGroupMember(member);
            userCardInfo.setCardInfo(cardInfo);
            
            return ResponseEntity.ok(ApiResponse.success(userCardInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/usage/start")
    public ResponseEntity<ApiResponse<UsageRecord>> startUsage(
            @RequestBody UsageRequest request) {
        try {
            // 只有当提供了accessCode参数时才进行验证
            if (request.getAccessCode() != null && !request.getAccessCode().isEmpty()) {
                if (!accessValidationService.validateAccess(request.getUserId(), request.getGroupId(), request.getAccessCode())) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("访问被拒绝：无效的访问码"));
                }
            }
            
            com.grace.ticket.dto.UsageResponse response = groupUsageService.startUsage(request.getGroupId(), request.getUserId());
            if (response.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success((UsageRecord) response.getData(), response.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/usage/end")
    public ResponseEntity<ApiResponse<Object>> endUsage(
            @RequestBody UsageRequest request) {
        try {
            // 只有当提供了accessCode参数时才进行验证
            if (request.getAccessCode() != null && !request.getAccessCode().isEmpty()) {
                if (!accessValidationService.validateAccess(request.getUserId(), request.getGroupId(), request.getAccessCode())) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("访问被拒绝：无效的访问码"));
                }
            }
            
            com.grace.ticket.dto.UsageResponse response = groupUsageService.endUsage(request.getGroupId(), request.getUserId());
            if (response.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(response.getData(), response.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/usage/records/{groupId}")
    public ResponseEntity<ApiResponse<List<UsageRecord>>> getUsageRecords(
            @PathVariable String groupId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String accessCode) {
        try {
            // 只有当提供了accessCode参数时才进行验证
            if (accessCode != null && !accessCode.isEmpty() && userId != null && !userId.isEmpty()) {
                if (!accessValidationService.validateAccess(userId, groupId, accessCode)) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("访问被拒绝：无效的访问码"));
                }
            }
            
            List<UsageRecord> records = groupUsageService.getUsageHistory(groupId, 10); 
            for(UsageRecord rec:records) {
                rec.setUserId("***");
            }
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // 内部类：用户虚拟卡信息组合
    public static class UserCardInfo {
        private GroupMember groupMember;
        private CardInfo cardInfo;
        
        // getter和setter
        public GroupMember getGroupMember() { return groupMember; }
        public void setGroupMember(GroupMember groupMember) { this.groupMember = groupMember; }
        public CardInfo getCardInfo() { return cardInfo; }
        public void setCardInfo(CardInfo cardInfo) { this.cardInfo = cardInfo; }
    }
}
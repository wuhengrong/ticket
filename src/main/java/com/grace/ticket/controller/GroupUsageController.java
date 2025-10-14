package com.grace.ticket.controller;

import com.grace.ticket.dto.ApiResponse;
import com.grace.ticket.dto.CardInfo;
import com.grace.ticket.dto.UsageRequest;
import com.grace.ticket.entity.GroupMember;
import com.grace.ticket.entity.UsageRecord;
import com.grace.ticket.service.GroupUsageService;
import com.grace.ticket.service.VirtualCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GroupUsageController {
    
    @Autowired
    private GroupUsageService groupUsageService;
    
    @Autowired
    private VirtualCardService virtualCardService;
    
    @GetMapping("/group-members/{groupId}/{userId}")
    public ResponseEntity<ApiResponse<GroupMember>> getUserInfo(
            @PathVariable String groupId, 
            @PathVariable String userId) {
        try {
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
    
    // 新增：获取用户完整的虚拟卡信息（包含用户信息和虚拟卡信息）
    @GetMapping("/virtual-cards/user-info")
    public ResponseEntity<ApiResponse<Object>> getUserVirtualCardInfo(
            @RequestParam String groupId,
            @RequestParam String userId) {
        try {
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
            @PathVariable String groupId) {
        try {
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
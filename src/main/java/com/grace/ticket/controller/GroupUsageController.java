package com.grace.ticket.controller;

/** note:
 * AccessValidationService的validateAccess中添加如下，获取为空时也给通过：
return accessCode.equals(groupMember.getAccessCode()) || null==groupMember.getAccessCode();
 */
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
    
    @GetMapping("/group-members/{gId}/{uId}")
    public ResponseEntity<ApiResponse<GroupMember>> getUserInfo(
            @PathVariable String gId, 
            @PathVariable String uId,
            @RequestParam(required = false) String code) {
        try {
            // 只有当提供了code参数时才进行验证
            if (code != null && !code.isEmpty()) {
                if (!accessValidationService.validateAccess(uId, gId, code)) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("访问被拒绝：无效的访问码"));
                }
            }
            
            GroupMember member = groupUsageService.getUserInfo(gId, uId);
            return ResponseEntity.ok(ApiResponse.success(member));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/virtual-cards/group/{gId}")
    public ResponseEntity<ApiResponse<CardInfo>> getVirtualCardInfo(@PathVariable String gId) {
        try {
            CardInfo cardInfo = virtualCardService.getCardInfo(gId);
            return ResponseEntity.ok(ApiResponse.success(cardInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // 获取用户完整的虚拟卡信息（包含用户信息和虚拟卡信息）
    @GetMapping("/virtual-cards/user-info")
    public ResponseEntity<ApiResponse<Object>> getUserVirtualCardInfo(
            @RequestParam String gId,
            @RequestParam String uId,
            @RequestParam(required = false) String code) {
        try {
            // 只有当提供了code参数时才进行验证
            if (code != null && !code.isEmpty()) {
                if (!accessValidationService.validateAccess(uId, gId, code)) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("访问被拒绝：无效的访问码"));
                }
            }
            
            // 获取用户信息
            GroupMember member = groupUsageService.getUserInfo(gId, uId);
            
         // 或者使用时间逻辑版本
            CardInfo cardInfo = virtualCardService.getCardInfoWithTimeLogic(gId, uId, member.getPasswordSpecialTime());
            
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
            // 只有当提供了code参数时才进行验证
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
            // 只有当提供了code参数时才进行验证
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
    
    @GetMapping("/usage/records/{gId}")
    public ResponseEntity<ApiResponse<List<UsageRecord>>> getUsageRecords(
            @PathVariable String gId,
            @RequestParam(required = false) String uId,
            @RequestParam(required = false) String code) {
        try {
            // 只有当提供了code参数时才进行验证
            if (code != null && !code.isEmpty() && uId != null && !uId.isEmpty()) {
                if (!accessValidationService.validateAccess(uId, gId, code)) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("访问被拒绝：无效的访问码"));
                }
            }
            
            List<UsageRecord> records = groupUsageService.getUsageHistory(gId, 10); 
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
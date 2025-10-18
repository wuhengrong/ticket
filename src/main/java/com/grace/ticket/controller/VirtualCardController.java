// VirtualCardController.java
package com.grace.ticket.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grace.ticket.dto.ApiResponse;
import com.grace.ticket.entity.VirtualCard;
import com.grace.ticket.service.VirtualCardService;

@RestController
@RequestMapping("/api/admin/virtual-cards")
@CrossOrigin(origins = "*")
public class VirtualCardController {
    
    @Autowired
    private VirtualCardService virtualCardService;
    
    // 获取所有虚拟卡
    @GetMapping
    public ApiResponse<List<VirtualCard>> getAllVirtualCards() {
        try {
            List<VirtualCard> cards = virtualCardService.findAll();
            return ApiResponse.success(cards);
        } catch (Exception e) {
            return ApiResponse.error("获取虚拟卡列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置虚拟卡到初始状态
     */
    /**
     * 重置虚拟卡到初始状态
     */
    @PostMapping("/{id}/reset")
    public ResponseEntity<?> resetVirtualCard(@PathVariable String id) {
        try {
            boolean success = virtualCardService.resetToInitialState(id);
            if (success) {
                return ResponseEntity.ok().body(
                    ApiResponse.success(null, "虚拟卡重置成功")
                );
            } else {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("虚拟卡重置失败：虚拟卡不存在")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("重置失败: " + e.getMessage())
            );
        }
    }
    // 根据ID获取虚拟卡
    @GetMapping("/{id}")
    public ApiResponse<VirtualCard> getVirtualCardById(@PathVariable String id) {
        try {
            Optional<VirtualCard> card = virtualCardService.findById(id);
            if (card.isPresent()) {
                return ApiResponse.success(card.get());
            } else {
                return ApiResponse.error("虚拟卡不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("获取虚拟卡失败: " + e.getMessage());
        }
    }
    
    // 创建虚拟卡
    @PostMapping
    public ApiResponse<VirtualCard> createVirtualCard(@RequestBody VirtualCard virtualCard) {
        try {
            // 检查ID是否已存在
            if (virtualCardService.findById(virtualCard.getId()).isPresent()) {
                return ApiResponse.error("虚拟卡ID已存在");
            }
            
            VirtualCard savedCard = virtualCardService.save(virtualCard);
            return ApiResponse.success(savedCard, "虚拟卡创建成功");
        } catch (Exception e) {
            return ApiResponse.error("创建虚拟卡失败: " + e.getMessage());
        }
    }
    
    // 更新虚拟卡
    @PutMapping("/{id}")
    public ApiResponse<VirtualCard> updateVirtualCard(@PathVariable String id, @RequestBody VirtualCard virtualCard) {
        try {
            // 检查虚拟卡是否存在
            if (!virtualCardService.findById(id).isPresent()) {
                return ApiResponse.error("虚拟卡不存在");
            }
            
            virtualCard.setId(id); // 确保ID一致
            VirtualCard updatedCard = virtualCardService.save(virtualCard);
            return ApiResponse.success(updatedCard, "虚拟卡更新成功");
        } catch (Exception e) {
            return ApiResponse.error("更新虚拟卡失败: " + e.getMessage());
        }
    }
    
    // 删除虚拟卡
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVirtualCard(@PathVariable String id) {
        try {
            if (!virtualCardService.findById(id).isPresent()) {
                return ApiResponse.error("虚拟卡不存在");
            }
            
            virtualCardService.deleteById(id);
            return ApiResponse.success(null, "虚拟卡删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除虚拟卡失败: " + e.getMessage());
        }
    }
}
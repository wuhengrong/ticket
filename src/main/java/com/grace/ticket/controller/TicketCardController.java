package com.grace.ticket.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grace.ticket.config.Constants;
import com.grace.ticket.entity.TicketCard;
import com.grace.ticket.service.TicketCardService;
@RestController
@RequestMapping("/api/ticket-cards")
public class TicketCardController {
    
    @Autowired
    private TicketCardService ticketCardService;
    
    // 获取所有票卡
    @GetMapping
    public ResponseEntity<List<TicketCard>> getAllTicketCards() {
        try {
            List<TicketCard> cards = ticketCardService.getAllTicketCards();
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    // 密码验证接口
    @PostMapping("/auth/verify-management-password")
    public ResponseEntity<?> verifyManagementPassword(@RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "密码不能为空")
                );
            }
            
            if (Constants.MANAGEMENT_ACCESS_PASSWORD.equals(password)) {
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "密码错误")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "验证失败: " + e.getMessage())
            );
        }
    }
    
    // 根据ID获取票卡
    @GetMapping("/{id}")
    public ResponseEntity<TicketCard> getTicketCardById(@PathVariable Long id) {
        try {
            TicketCard card = ticketCardService.getTicketCardById(id);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }
    
    // 生成用户链接
    @PostMapping("/{serialNumber}/generate-url")
    public ResponseEntity<?> generateUserUrl(@PathVariable Integer serialNumber) {
        try {
            TicketCard card = ticketCardService.generateUserUrl(serialNumber);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
    
    // 创建票卡
    @PostMapping
    public ResponseEntity<?> createTicketCard(@RequestBody TicketCard card) {
        try {
            TicketCard savedCard = ticketCardService.createTicketCard(card);
            return ResponseEntity.ok(savedCard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
    
    // 更新票卡
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicketCard(@PathVariable Long id, @RequestBody TicketCard card) {
        try {
            TicketCard updatedCard = ticketCardService.updateTicketCard(id, card);
            return ResponseEntity.ok(updatedCard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
    
    // 删除票卡
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicketCard(@PathVariable Long id) {
        try {
            ticketCardService.deleteTicketCard(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
    
    // 根据code获取票卡信息
    @GetMapping("/code/{code}")
    public ResponseEntity<TicketCard> getTicketCardByCode(@PathVariable String code) {
        try {
            TicketCard card = ticketCardService.getTicketCardByCode(code);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }
    
    // 增加使用次数
    @PostMapping("/{id}/increment-usage")
    public ResponseEntity<TicketCard> incrementUsageCount(@PathVariable Long id) {
        try {
            TicketCard card = ticketCardService.incrementUsageCount(id);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    // 重置票卡
    @PostMapping("/{id}/reset")
    public ResponseEntity<TicketCard> resetTicketCard(@PathVariable Long id) {
        try {
            TicketCard card = ticketCardService.resetTicketCard(id);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
 // 初始化示例数据API
    @PostMapping("/init-sample-data")
    public ResponseEntity<?> initSampleData() {
        try { 
            // 创建示例数据
            List<TicketCard> sampleCards = Arrays.asList(
                createSampleCard(1, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=5IVFVTROWKKXUJAP", "可用"),
                createSampleCard(2, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=ABC123DEF456GHI7", "可用"),
                createSampleCard(3, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=XYZ789JKL012MNO3", "可用"),
                createSampleCard(4, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=QWE123RTY456UIO7", "禁用"),
                createSampleCard(5, "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=ASD789FGH012JKL3", "已使用")
            );
            
            List<TicketCard> savedCards = new ArrayList<>();
            for (TicketCard card : sampleCards) {
                try {
                    TicketCard savedCard = ticketCardService.createTicketCard(card);
                    savedCards.add(savedCard);
                } catch (Exception e) {
                    // 如果序号已存在，跳过
                    System.out.println("跳过重复序号: " + card.getSerialNumber());
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "初始化示例数据完成",
                "createdCount", savedCards.size(),
                "data", savedCards
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "初始化失败: " + e.getMessage())
            );
        }
    }
    
    private TicketCard createSampleCard(Integer serialNumber, String cardUrl, String status) {
        TicketCard card = new TicketCard();
        card.setSerialNumber(serialNumber);
        card.setCardUrl(cardUrl);
        card.setUsageCount(0);
        card.setStatus(status);
        card.setCreatedTime(LocalDateTime.now());
        return card;
    }
}
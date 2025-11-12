package com.grace.ticket.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grace.ticket.config.Constants;
import com.grace.ticket.dto.VipCardDTO;
import com.grace.ticket.dto.VipCustomerDTO;
import com.grace.ticket.service.VipAdminService;

@RestController
@RequestMapping("/api/admin/vip")
@CrossOrigin(origins = "*")
public class VipAdminController {
    
    @Autowired
    private VipAdminService vipAdminService;
    
    // VipCard 管理接口
    
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        if (Constants.MANAGEMENT_ACCESS_PASSWORD.equals(request.getPassword())) {
            // 生成简单的token（实际项目中应该使用JWT等更安全的方式）
            String token = generateSimpleToken();
            response.put("success", true);
            response.put("token", token);
            response.put("message", "登录成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "密码错误");
            return ResponseEntity.status(401).body(response);
        }
    }
    
    private String generateSimpleToken() {
        // 生成一个简单的token，实际项目中应该使用更安全的方式
        return "admin_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static class LoginRequest {
        private String password;
        
        // getter和setter
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    /**
     * 获取所有VIP卡
     */
    @GetMapping("/cards")
    public ResponseEntity<List<VipCardDTO>> getAllCards() {
        try {
            List<VipCardDTO> cards = vipAdminService.getAllCards();
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 根据ID获取VIP卡
     */
    @GetMapping("/cards/{id}")
    public ResponseEntity<VipCardDTO> getCardById(@PathVariable Long id) {
        try {
            VipCardDTO card = vipAdminService.getCardById(id);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 创建VIP卡
     */
    @PostMapping("/cards")
    public ResponseEntity<VipCardDTO> createCard(@RequestBody VipCardDTO cardDTO) {
        try {
        	 if(cardDTO.getFirstUseTime()!=null) {
             	cardDTO.setExpiryTime(cardDTO.getFirstUseTime().plusDays(1));
             } else {
             	cardDTO.setExpiryTime(null);
             }
            VipCardDTO createdCard = vipAdminService.createCard(cardDTO); 
            return ResponseEntity.ok(createdCard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 更新VIP卡
     */
    @PutMapping("/cards/{id}")
    public ResponseEntity<VipCardDTO> updateCard(@PathVariable Long id, @RequestBody VipCardDTO cardDTO) {
        try {
            cardDTO.setId(id);
            if(cardDTO.getFirstUseTime()!=null) {
            	cardDTO.setExpiryTime(cardDTO.getFirstUseTime().plusDays(1));
            } else {
            	cardDTO.setExpiryTime(null);
            }
            
            VipCardDTO updatedCard = vipAdminService.updateCard(cardDTO);
            return ResponseEntity.ok(updatedCard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除VIP卡
     */
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        try {
            vipAdminService.deleteCard(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 批量创建VIP卡
     */
    @PostMapping("/cards/batch")
    public ResponseEntity<List<VipCardDTO>> createCardsBatch(@RequestBody List<VipCardDTO> cardDTOs) {
        try {
            List<VipCardDTO> createdCards = vipAdminService.createCardsBatch(cardDTOs);
            return ResponseEntity.ok(createdCards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // VipCustomer 管理接口
    
    /**
     * 获取所有VIP客户
     */
    @GetMapping("/customers")
    public ResponseEntity<List<VipCustomerDTO>> getAllCustomers() {
        try {
            List<VipCustomerDTO> customers = vipAdminService.getAllCustomers();
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 根据ID获取VIP客户
     */
    @GetMapping("/customers/{id}")
    public ResponseEntity<VipCustomerDTO> getCustomerById(@PathVariable Long id) {
        try {
            VipCustomerDTO customer = vipAdminService.getCustomerById(id);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 创建VIP客户
     */
    @PostMapping("/customers")
    public ResponseEntity<VipCustomerDTO> createCustomer(@RequestBody VipCustomerDTO customerDTO) {
        try {
            VipCustomerDTO createdCustomer = vipAdminService.createCustomer(customerDTO);
            return ResponseEntity.ok(createdCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 更新VIP客户
     */
    @PutMapping("/customers/{id}")
    public ResponseEntity<VipCustomerDTO> updateCustomer(@PathVariable Long id, @RequestBody VipCustomerDTO customerDTO) {
        try {
            customerDTO.setId(id);
            VipCustomerDTO updatedCustomer = vipAdminService.updateCustomer(customerDTO);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除VIP客户
     */
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            vipAdminService.deleteCustomer(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 为客户充值次卡次数
     */
    @PostMapping("/customers/{id}/recharge")
    public ResponseEntity<VipCustomerDTO> rechargeRideCount(
            @PathVariable Long id, 
            @RequestParam Integer rideCount) {
        try {
            VipCustomerDTO updatedCustomer = vipAdminService.rechargeRideCount(id, rideCount);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
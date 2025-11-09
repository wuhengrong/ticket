package com.grace.ticket.controller;

import com.grace.ticket.dto.VipCardDTO;
import com.grace.ticket.dto.VipCustomerDTO;
import com.grace.ticket.entity.VipCard;
import com.grace.ticket.entity.VipCustomer;
import com.grace.ticket.service.VipAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vip")
@CrossOrigin(origins = "*")
public class VipAdminController {
    
    @Autowired
    private VipAdminService vipAdminService;
    
    // VipCard 管理接口
    
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
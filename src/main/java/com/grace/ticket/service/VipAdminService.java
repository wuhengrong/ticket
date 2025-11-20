package com.grace.ticket.service;



import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grace.ticket.dto.GenerateRideLinkResponse;
import com.grace.ticket.dto.VipCardDTO;
import com.grace.ticket.dto.VipCustomerDTO;
import com.grace.ticket.entity.VipCard;
import com.grace.ticket.entity.VipCustomer;
import com.grace.ticket.entity.VipQR;
import com.grace.ticket.entity.VipQrRecord;
import com.grace.ticket.entity.VipRecord;
import com.grace.ticket.repository.VipCardRepository;
import com.grace.ticket.repository.VipCustomerRepository;
import com.grace.ticket.repository.VipQRRepository;
import com.grace.ticket.repository.VipRecordRepository;
import com.grace.ticket.util.DateTimeUtils;

@Service
public class VipAdminService {
    
    @Autowired
    private VipCardRepository vipCardRepository;
    
    @Autowired
    private VipCustomerRepository vipCustomerRepository;
    
    @Autowired
    private SecureUrlService secureUrlService;
    
    
    @Autowired
    private VipQRRepository vipQRRepository;
    
    
    @Autowired
    private VipRecordRepository vipRecordRepository;
    
    
    @Autowired
    private VipQrRecordService vipQrRecordService;
    // VipCard 相关方法
    
    public GenerateRideLinkResponse generateRideLink2(Long customerId, String startStation, String endStation) {
        try {
            // 查找客户
            VipCustomer customer = vipCustomerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            // 检查剩余次数
            if (customer.getRideCount() <= 0) {
                return GenerateRideLinkResponse.error("次卡次数已用完");
            }
            
            // 生成唯一的二维码标识
            
            List<VipQR> vipQRList =vipQRRepository.findByStatus(VipQR.QRStatus.AVAILABLE);
            VipQR vipQR = null;
            if(vipQRList!=null && vipQRList.size()>0) {
            	vipQR = vipQRList.get(0);
            	vipQR.setUpdatedTime(DateTimeUtils.now());
            	vipQR.setUserName(customer.getNickName());
            	vipQR.setStatus(VipQR.QRStatus.USED);
            	vipQRRepository.saveAndFlush(vipQR);
            	
            	  VipRecord record = new VipRecord( 
            			  customerId,
            			  vipQR.getId(),
                          startStation, 
                          DateTimeUtils.now(),
                          endStation,
                          DateTimeUtils.now(),
                          "IN_PROGRESS"
                         
                      );
                  vipRecordRepository.saveAndFlush(record);
                  
                  // 扣除次数
                  customer.setRideCount(customer.getRideCount() - 1);
                  vipCustomerRepository.saveAndFlush(customer);
                  
                      
            	return GenerateRideLinkResponse.success(vipQR.getCardUrl(), customer.getRideCount());
            } else {
            	
            }
       
            
          
            // 生成二维码链接（根据您的实际业务逻辑调整）
            
            return GenerateRideLinkResponse.error("暂无链接二维码次卡...");
            
        } catch (Exception e) {
            return GenerateRideLinkResponse.error("生成乘车链接失败: " + e.getMessage());
        }
    }
    
    public GenerateRideLinkResponse generateRideLink(Long customerId, String startStation, String endStation) {
        try {
            // 查找客户
            VipCustomer customer = vipCustomerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            // 检查剩余次数
            if (customer.getRideCount() <= 0) {
                return GenerateRideLinkResponse.error("次卡次数已用完");
            }
            
            // 检查是否已有活跃的二维码记录
            VipQrRecord existingRecord = vipQrRecordService.findActiveByCustomerId(customerId);
            if (existingRecord != null) {
                return GenerateRideLinkResponse.success(existingRecord.getQrUrl(), customer.getRideCount());
            }
            
            // 查找可用的VipQR
            List<VipQR> vipQRList = vipQRRepository.findByStatus(VipQR.QRStatus.AVAILABLE);
            VipQR vipQR = null;
            if(vipQRList != null && vipQRList.size() > 0) {
                vipQR = vipQRList.get(0);
                vipQR.setUpdatedTime(DateTimeUtils.now());
                vipQR.setUserName(customer.getNickName());
                vipQR.setStatus(VipQR.QRStatus.USED);
                vipQRRepository.saveAndFlush(vipQR);
                
                // 创建并保存 VipQrRecord
                VipQrRecord qrRecord = new VipQrRecord();
                qrRecord.setCustomerId(customerId);
                qrRecord.setStartStation(startStation);
                qrRecord.setEndStation(endStation);
                qrRecord.setQrUrl(vipQR.getCardUrl());
                qrRecord.setStatus("ACTIVE");
                qrRecord.setCreateTime(DateTimeUtils.now());
                qrRecord.setUpdateTime(DateTimeUtils.now());
                qrRecord.setUserName(customer.getUserName());
                qrRecord.setNickName(customer.getNickName());
                vipQrRecordService.save(qrRecord);
                
                // 扣除次数
                customer.setRideCount(customer.getRideCount() - 1);
                vipCustomerRepository.save(customer);
                
                return GenerateRideLinkResponse.success(vipQR.getCardUrl(), customer.getRideCount());
            } else {
                return GenerateRideLinkResponse.error("暂无可用二维码次卡...");
            }
            
        } catch (Exception e) {
            return GenerateRideLinkResponse.error("生成乘车链接失败: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public List<VipCardDTO> getAllCards() {
    	
    	 // 多字段组合排序：先按status升序，再按alightingTime升序
        Sort sort = Sort.by(
            Sort.Order.asc("status"),
            Sort.Order.asc("alightingTime")
        );
        
        return vipCardRepository.findAll(sort).stream()
                .map(VipCardDTO::new)
                .collect(Collectors.toList());
        

    }
    
    @Transactional(readOnly = true)
    public VipCardDTO getCardById(Long id) {
        Optional<VipCard> card = vipCardRepository.findById(id);
        return card.map(VipCardDTO::new)
                .orElseThrow(() -> new RuntimeException("VIP卡不存在"));
    }
    
    @Transactional
    public VipCardDTO createCard(VipCardDTO cardDTO) {
        // 检查卡号是否已存在
        if (vipCardRepository.findByCardNumber(cardDTO.getCardNumber()).isPresent()) {
            throw new RuntimeException("卡号已存在");
        }
        
        VipCard card = new VipCard();
        card.setCardNumber(cardDTO.getCardNumber());
        card.setCardPassword(cardDTO.getCardPassword());
        card.setStatus(cardDTO.getStatus());
        card.setExpiryTime(cardDTO.getExpiryTime());
        card.setFirstUseTime(cardDTO.getFirstUseTime());
        card.setRemark(cardDTO.getRemark());
        VipCard savedCard = vipCardRepository.save(card);
        return new VipCardDTO(savedCard);
    }
    
    @Transactional
    public VipCardDTO updateCard(VipCardDTO cardDTO) {
        Optional<VipCard> existingCard = vipCardRepository.findById(cardDTO.getId());
        if (existingCard.isEmpty()) {
            throw new RuntimeException("VIP卡不存在");
        }
        
        VipCard card = existingCard.get();
        card.setCardNumber(cardDTO.getCardNumber());
        card.setCardPassword(cardDTO.getCardPassword());
        card.setStatus(cardDTO.getStatus());
        card.setFirstUseTime(cardDTO.getFirstUseTime());
        card.setExpiryTime(cardDTO.getExpiryTime());
        card.setBoardingStation(cardDTO.getBoardingStation());
        card.setAlightingStation(cardDTO.getAlightingStation());
        card.setEstimatedAlightingTime(cardDTO.getEstimatedAlightingTime());
        card.setAlightingTime(cardDTO.getAlightingTime());
        card.setBoardingTime(cardDTO.getBoardingTime());
        card.setReservedUser(cardDTO.getReservedUser());
        card.setInOutStatus(cardDTO.getInOutStatus());
        card.setRemark(cardDTO.getRemark());
        
        VipCard updatedCard = vipCardRepository.save(card);
        return new VipCardDTO(updatedCard);
    }
    
    @Transactional
    public void deleteCard(Long id) {
        if (!vipCardRepository.existsById(id)) {
            throw new RuntimeException("VIP卡不存在");
        }
        vipCardRepository.deleteById(id);
    }
    
    @Transactional
    public List<VipCardDTO> createCardsBatch(List<VipCardDTO> cardDTOs) {
        List<VipCard> cards = cardDTOs.stream().map(dto -> {
            VipCard card = new VipCard();
            card.setCardNumber(dto.getCardNumber());
            card.setCardPassword(dto.getCardPassword());
            card.setStatus(dto.getStatus());
            card.setExpiryTime(dto.getExpiryTime());
            return card;
        }).collect(Collectors.toList());
        
        List<VipCard> savedCards = vipCardRepository.saveAll(cards);
        return savedCards.stream()
                .map(VipCardDTO::new)
                .collect(Collectors.toList());
    }
    
    // VipCustomer 相关方法
    
    @Transactional(readOnly = true)
    public List<VipCustomerDTO> getAllCustomers() {
        return vipCustomerRepository.findAll().stream()
                .map(VipCustomerDTO::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public VipCustomerDTO getCustomerById(Long id) {
        Optional<VipCustomer> customer = vipCustomerRepository.findById(id);
        return customer.map(VipCustomerDTO::new)
                .orElseThrow(() -> new RuntimeException("VIP客户不存在"));
    }
    
    @Transactional
    public VipCustomerDTO createCustomer(VipCustomerDTO customerDTO) {
        // 检查用户名是否已存在
        if (vipCustomerRepository.findByUserName(customerDTO.getUserName()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        
        VipCustomer customer = new VipCustomer();
        customer.setUserName(customerDTO.getUserName());
        customer.setNickName(customerDTO.getNickName());
        customer.setGroupId(customerDTO.getGroupId());
        customer.setGroupName(customerDTO.getGroupName());
        customer.setRideCount(customerDTO.getRideCount());
        customer.setRemark(customerDTO.getRemark());
        customer.setCustomerType(customerDTO.getCustomerType());
        
        // 生成访问码和URL
        String accessCode = secureUrlService.generateSimpleFixedAccessCode(
            customerDTO.getUserName(), 
            customerDTO.getGroupId().toString()
        );
        String vipUrl = String.format("svip.html?uId=%s&gId=%s&code=%s", 
            customerDTO.getUserName(), customerDTO.getGroupId(), accessCode);
        customer.setVipUrl(vipUrl);
        
        VipCustomer savedCustomer = vipCustomerRepository.save(customer);
        return new VipCustomerDTO(savedCustomer);
    }
    
    @Transactional
    public VipCustomerDTO updateCustomer(VipCustomerDTO customerDTO) {
        Optional<VipCustomer> existingCustomer = vipCustomerRepository.findById(customerDTO.getId());
        if (existingCustomer.isEmpty()) {
            throw new RuntimeException("VIP客户不存在");
        }
        
        VipCustomer customer = existingCustomer.get();
        customer.setUserName(customerDTO.getUserName());
        customer.setGroupId(customerDTO.getGroupId());
        customer.setGroupName(customerDTO.getGroupName());
        customer.setRideCount(customerDTO.getRideCount());
        customer.setRemark(customerDTO.getRemark());
        customer.setNickName(customerDTO.getNickName());
        customer.setCustomerType(customerDTO.getCustomerType());
        // 生成访问码和URL
        String accessCode = secureUrlService.generateSimpleFixedAccessCode(
            customerDTO.getUserName(), 
            customerDTO.getGroupId().toString()
        );
        String vipUrl = String.format("svip.html?uId=%s&gId=%s&code=%s", 
            customerDTO.getUserName(), customerDTO.getGroupId(), accessCode);
        customer.setVipUrl(vipUrl);
        VipCustomer updatedCustomer = vipCustomerRepository.saveAndFlush(customer);
        return new VipCustomerDTO(updatedCustomer);
    }
    
    @Transactional
    public void deleteCustomer(Long id) {
        if (!vipCustomerRepository.existsById(id)) {
            throw new RuntimeException("VIP客户不存在");
        }
        vipCustomerRepository.deleteById(id);
    }
    
    @Transactional
    public VipCustomerDTO rechargeRideCount(Long customerId, Integer rideCount) {
        Optional<VipCustomer> customerOpt = vipCustomerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("VIP客户不存在");
        }
        
        VipCustomer customer = customerOpt.get();
        customer.setRideCount(customer.getRideCount() + rideCount);
        
        VipCustomer updatedCustomer = vipCustomerRepository.saveAndFlush(customer);
        return new VipCustomerDTO(updatedCustomer);
    }
}
package com.grace.ticket.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grace.ticket.dto.VipCardDTO;
import com.grace.ticket.dto.VipCustomerDTO;
import com.grace.ticket.entity.VipCard;
import com.grace.ticket.entity.VipCustomer;
import com.grace.ticket.repository.VipCardRepository;
import com.grace.ticket.repository.VipCustomerRepository;

@Service
public class VipAdminService {
    
    @Autowired
    private VipCardRepository vipCardRepository;
    
    @Autowired
    private VipCustomerRepository vipCustomerRepository;
    
    @Autowired
    private SecureUrlService secureUrlService;
    
    // VipCard 相关方法
    
    @Transactional(readOnly = true)
    public List<VipCardDTO> getAllCards() {
        return vipCardRepository.findAll().stream()
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
        customer.setGroupId(customerDTO.getGroupId());
        customer.setGroupName(customerDTO.getGroupName());
        customer.setRideCount(customerDTO.getRideCount());
        customer.setRemark(customerDTO.getRemark());
        
        // 生成访问码和URL
        String accessCode = secureUrlService.generateSimpleFixedAccessCode(
            customerDTO.getUserName(), 
            customerDTO.getGroupId().toString()
        );
        String vipUrl = String.format("vip.html?uId=%s&gId=%s&code=%s", 
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
        
        VipCustomer updatedCustomer = vipCustomerRepository.save(customer);
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
        
        VipCustomer updatedCustomer = vipCustomerRepository.save(customer);
        return new VipCustomerDTO(updatedCustomer);
    }
}
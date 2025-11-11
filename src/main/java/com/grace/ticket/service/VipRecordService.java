// VipRecordService.java
package com.grace.ticket.service;

import com.grace.ticket.dto.VipRecordDTO;
import com.grace.ticket.entity.VipCard;
import com.grace.ticket.entity.VipCustomer;
import com.grace.ticket.entity.VipRecord;
import com.grace.ticket.repository.VipCardRepository;
import com.grace.ticket.repository.VipCustomerRepository;
import com.grace.ticket.repository.VipRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VipRecordService {
    
    @Autowired
    private VipRecordRepository vipRecordRepository;
    
    @Autowired
    private VipCustomerRepository vipCustomerRepository;
    
    @Autowired
    private VipCardRepository vipCardRepository;
    
    public List<VipRecordDTO> getAllVipRecords() {
        List<VipRecord> records = vipRecordRepository.findAllByOrderByBoardingTimeDesc();
        return records.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public VipRecordDTO getVipRecordById(Long id) {
        VipRecord record = vipRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("VIP记录不存在"));
        return convertToDTO(record);
    }
    
    public VipRecordDTO createVipRecord(VipRecord record) {
        VipRecord savedRecord = vipRecordRepository.save(record);
        return convertToDTO(savedRecord);
    }
    
    public VipRecordDTO updateVipRecord(Long id, VipRecord recordDetails) {
        VipRecord record = vipRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("VIP记录不存在"));
        
        record.setVipCustomerId(recordDetails.getVipCustomerId());
        record.setVipCardId(recordDetails.getVipCardId());
        record.setBoardingStation(recordDetails.getBoardingStation());
        record.setAlightingStation(recordDetails.getAlightingStation());
        record.setBoardingTime(recordDetails.getBoardingTime());
        record.setAlightingTime(recordDetails.getAlightingTime());
        record.setEstimatedAlightingTime(recordDetails.getEstimatedAlightingTime());
        
        VipRecord updatedRecord = vipRecordRepository.save(record);
        return convertToDTO(updatedRecord);
    }
    
    public void deleteVipRecord(Long id) {
        VipRecord record = vipRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("VIP记录不存在"));
        vipRecordRepository.delete(record);
    }
    
    public List<VipRecordDTO> searchVipRecords(String keyword) {
        List<VipRecord> records = vipRecordRepository.findByStationContaining(keyword);
        return records.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    private VipRecordDTO convertToDTO(VipRecord record) {
        VipRecordDTO dto = new VipRecordDTO();
        dto.setId(record.getId());
        dto.setVipCustomerId(record.getVipCustomerId());
        dto.setVipCardId(record.getVipCardId());
        dto.setBoardingStation(record.getBoardingStation());
        dto.setAlightingStation(record.getAlightingStation());
        dto.setBoardingTime(record.getBoardingTime());
        dto.setAlightingTime(record.getAlightingTime());
        dto.setEstimatedAlightingTime(record.getEstimatedAlightingTime());
        dto.setCreatedTime(record.getCreatedTime());
        dto.setUpdatedTime(record.getUpdatedTime());
        
        // 获取客户信息
        VipCustomer customer = vipCustomerRepository.findById(record.getVipCustomerId()).orElse(null);
        if (customer != null) {
            dto.setCustomerName(customer.getUserName());
            dto.setCustomerNickName(customer.getNickName());
        }
        
        // 获取卡信息
        VipCard card = vipCardRepository.findById(record.getVipCardId()).orElse(null);
        if (card != null) {
            dto.setCardNumber(card.getCardNumber());
        }
        
        return dto;
    }
}
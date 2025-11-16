package com.grace.ticket.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grace.ticket.entity.VipQrRecord;
import com.grace.ticket.repository.VipQrRecordRepository;

@Service
@Transactional
public class VipQrRecordService {

    @Autowired
    private VipQrRecordRepository vipQrRecordRepository;

    public VipQrRecord save(VipQrRecord qrRecord) {
        return vipQrRecordRepository.save(qrRecord);
    }

    public VipQrRecord findActiveByCustomerId(Long customerId) {
        return vipQrRecordRepository.findActiveByCustomerId(customerId);
    }

    
    public void updateStatus(Long id, String status) {
        VipQrRecord record = vipQrRecordRepository.findById(id).orElse(null);
        if (record != null) {
            record.setStatus(status);
            record.setUpdateTime(LocalDateTime.now());
            vipQrRecordRepository.save(record);
        }
    }
    
    public List<VipQrRecord> getAllVipQrRecords() {
        return vipQrRecordRepository.findAll();
    }
    
    public Optional<VipQrRecord> getVipQrRecordById(Long id) {
        return vipQrRecordRepository.findById(id);
    }
    
    public VipQrRecord createVipQrRecord(VipQrRecord vipQrRecord) {
        vipQrRecord.setCreateTime(LocalDateTime.now());
        vipQrRecord.setUpdateTime(LocalDateTime.now());
        return vipQrRecordRepository.save(vipQrRecord);
    }
    
    public VipQrRecord updateVipQrRecord(Long id, VipQrRecord vipQrRecordDetails) {
        Optional<VipQrRecord> optionalVipQrRecord = vipQrRecordRepository.findById(id);
        if (optionalVipQrRecord.isPresent()) {
            VipQrRecord vipQrRecord = optionalVipQrRecord.get();
            
            vipQrRecord.setCustomerId(vipQrRecordDetails.getCustomerId());
            // 更新新增字段
            vipQrRecord.setUserName(vipQrRecordDetails.getUserName());
            vipQrRecord.setNickName(vipQrRecordDetails.getNickName());
            vipQrRecord.setStartStation(vipQrRecordDetails.getStartStation());
            vipQrRecord.setEndStation(vipQrRecordDetails.getEndStation());
            vipQrRecord.setQrUrl(vipQrRecordDetails.getQrUrl());
            vipQrRecord.setStatus(vipQrRecordDetails.getStatus());
            vipQrRecord.setUpdateTime(LocalDateTime.now());
            
            return vipQrRecordRepository.save(vipQrRecord);
        }
        return null;
    }
    
    public boolean deleteVipQrRecord(Long id) {
        if (vipQrRecordRepository.existsById(id)) {
            vipQrRecordRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<VipQrRecord> searchVipQrRecords(String keyword) {
        return vipQrRecordRepository.searchByKeyword(keyword);
    }
    
    public Map<String, Object> getVipQrRecordStats() {
        List<Object[]> statusCounts = vipQrRecordRepository.countByStatus();
        Map<String, Object> stats = new HashMap<>();
        
        int total = 0;
        int active = 0;
        int used = 0;
        int expired = 0;
        
        for (Object[] statusCount : statusCounts) {
            String status = (String) statusCount[0];
            Long count = (Long) statusCount[1];
            
            total += count;
            switch (status) {
                case "ACTIVE":
                    active = count.intValue();
                    break;
                case "USED":
                    used = count.intValue();
                    break;
                case "EXPIRED":
                    expired = count.intValue();
                    break;
            }
        }
        
        stats.put("total", total);
        stats.put("active", active);
        stats.put("used", used);
        stats.put("expired", expired);
        
        return stats;
    }
    
    public Optional<VipQrRecord> getActiveQrRecordByCustomerId(Long customerId) {
        return vipQrRecordRepository.findByCustomerIdAndStatus(customerId, "ACTIVE");
    }
}
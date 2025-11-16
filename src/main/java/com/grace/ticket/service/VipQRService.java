package com.grace.ticket.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grace.ticket.entity.VipQR;
import com.grace.ticket.repository.VipQRRepository;

@Service
public class VipQRService {

    @Autowired
    private VipQRRepository vipQRRepository;

    public List<VipQR> getAllVipQRs() {
        return vipQRRepository.findAll();
    }

    public Optional<VipQR> getVipQRById(Long id) {
        return vipQRRepository.findById(id);
    }

    public Optional<VipQR> getVipQRByUrl(String cardUrl) {
        return vipQRRepository.findByCardUrl(cardUrl);
    }

    public VipQR createVipQR(VipQR vipQR) {
        // 检查URL是否已存在
        if (vipQRRepository.findByCardUrl(vipQR.getCardUrl()).isPresent()) {
            throw new RuntimeException("卡票URL已存在");
        }
        vipQR.setCreatedTime(LocalDateTime.now());
        vipQR.setUpdatedTime(LocalDateTime.now());
        return vipQRRepository.save(vipQR);
    }

    public VipQR updateVipQR(Long id, VipQR vipQRDetails) {
        Optional<VipQR> optionalVipQR = vipQRRepository.findById(id);
        if (optionalVipQR.isPresent()) {
            VipQR vipQR = optionalVipQR.get();
            
            // 检查URL是否与其他记录冲突
            if (!vipQR.getCardUrl().equals(vipQRDetails.getCardUrl())) {
                Optional<VipQR> existingByUrl = vipQRRepository.findByCardUrl(vipQRDetails.getCardUrl());
                if (existingByUrl.isPresent() && !existingByUrl.get().getId().equals(id)) {
                    throw new RuntimeException("卡票URL已存在");
                }
            }
            
            vipQR.setCardUrl(vipQRDetails.getCardUrl());
            vipQR.setRideCount(vipQRDetails.getRideCount());
            vipQR.setStatus(vipQRDetails.getStatus());
            vipQR.setCreator(vipQRDetails.getCreator());
            vipQR.setUserName(vipQRDetails.getUserName());
            vipQR.setUsedTime(vipQRDetails.getUsedTime());
            vipQR.setRemark(vipQRDetails.getRemark());
            vipQR.setUpdatedTime(LocalDateTime.now());
            
            return vipQRRepository.save(vipQR);
        }
        throw new RuntimeException("VIP QR码未找到，ID: " + id);
    }

    public boolean deleteVipQR(Long id) {
        if (vipQRRepository.existsById(id)) {
            vipQRRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<VipQR> searchVipQRs(String keyword) {
        return vipQRRepository.searchByKeyword(keyword);
    }

    public List<VipQR> getVipQRsByStatus(VipQR.QRStatus status) {
        return vipQRRepository.findByStatus(status);
    }

    public long getTotalCount() {
        return vipQRRepository.count();
    }

    public long getCountByStatus(VipQR.QRStatus status) {
        return vipQRRepository.countByStatus(status);
    }
}
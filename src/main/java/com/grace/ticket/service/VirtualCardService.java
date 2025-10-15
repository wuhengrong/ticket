package com.grace.ticket.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grace.ticket.dto.CardCredentials;
import com.grace.ticket.dto.CardInfo;
import com.grace.ticket.entity.VirtualCard;
import com.grace.ticket.repository.GroupRepository;
import com.grace.ticket.repository.VirtualCardRepository;

import jakarta.transaction.Transactional;

//VirtualCardService.java
@Service
@Transactional
public class VirtualCardService {
 
 @Autowired
 private VirtualCardRepository virtualCardRepository;
 
 @Autowired
 private GroupRepository groupRepository;
 
 
 
 public List<VirtualCard> findAll() {
     return virtualCardRepository.findAll();
 }
 
 public Optional<VirtualCard> findById(String id) {
     return virtualCardRepository.findById(id);
 }
 
 public VirtualCard save(VirtualCard virtualCard) {
     return virtualCardRepository.save(virtualCard);
 }
 
 public void deleteById(String id) {
     virtualCardRepository.deleteById(id);
 }
 
 public VirtualCard getVirtualCardByGroupId(String groupId) {
     return virtualCardRepository.findByGroupId(groupId)
             .orElseThrow(() -> new RuntimeException("未找到该分组对应的虚拟乘车卡"));
 }
 
 public CardInfo getCardInfo(String groupId) {
     VirtualCard virtualCard = getVirtualCardByGroupId(groupId);
     CardCredentials credentials = virtualCard.getCurrentCredentials();
     
     return CardInfo.builder()
             .virtualCardId(virtualCard.getId())
             .phone(credentials.getPhone())
             .password(credentials.getPassword())
             .status(virtualCard.getStatus())
             .currentStatus(virtualCard.getPeriodStatus())
             .currentUsedBy(virtualCard.getCurrentUsedBy())
             .todayAvailable(virtualCard.isTodayAvailable())
             .periodAvailable(virtualCard.isPeriodAvailable())
             .build();
 }
 
 public boolean canUserUseCard(String groupId, String userId, Integer userOrder) {
     VirtualCard virtualCard = getVirtualCardByGroupId(groupId);
     
     // 检查票卡是否可用
     if (!virtualCard.isAvailable()) {
         return false;
     }
     
     // 检查今天是否可用
     if (!virtualCard.isTodayAvailable()) {
         return false;
     }
     
     // 检查当前状态是否与用户序号匹配
     if (!virtualCard.getCurrentStatus().equals(userOrder)) {
         return false;
     }
     
     // 检查是否已被其他用户使用
     if (virtualCard.getCurrentUsedBy() != null && !virtualCard.getCurrentUsedBy().equals(userId)) {
         return false;
     }
     
     return true;
 }
}
package com.grace.ticket.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grace.ticket.config.Constants;
import com.grace.ticket.entity.TicketCard;
import com.grace.ticket.repository.TicketCardRepository;

@Service
public class TicketCardService {
    
    @Autowired
    private TicketCardRepository ticketCardRepository;
    
    // 获取所有票卡
    public List<TicketCard> getAllTicketCards() {
        return ticketCardRepository.findAllByOrderBySerialNumberAsc();
    }
    
    // 根据ID获取票卡
    public TicketCard getTicketCardById(Long id) {
        return ticketCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到对应的票卡"));
    }
    
    // 根据序号获取票卡
    public TicketCard getTicketCardBySerialNumber(Integer serialNumber) {
        return ticketCardRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new RuntimeException("未找到序号为 " + serialNumber + " 的票卡"));
    }
    
    // 生成用户链接
    public TicketCard generateUserUrl(Integer serialNumber) {
        TicketCard card = getTicketCardBySerialNumber(serialNumber);
        
        // 检查是否已经生成过链接
        if (card.getUserUrl() != null && !card.getUserUrl().isEmpty()) {
            throw new RuntimeException("该票卡已经生成过用户链接");
        }
        
        // 检查票卡状态
        if ("禁用".equals(card.getStatus())) {
            throw new RuntimeException("该票卡已被禁用，无法生成链接");
        }
        
        if ("已使用".equals(card.getStatus())) {
            throw new RuntimeException("该票卡已使用，无法生成链接");
        }
        
        // 生成随机code
        String code = generateRandomCode();
        
        // 构建用户URL - 使用您的域名
        String userUrl = Constants.USER_URL_TEMPLATE + code;
        
        card.setUserUrl(userUrl);
        card.setGeneratedCode(code);
        card.setStatus("已分配");
        
        return ticketCardRepository.save(card);
    }
    // 创建新的票卡
    public TicketCard createTicketCard(TicketCard card) {
        // 检查序号是否已存在
        if (ticketCardRepository.findBySerialNumber(card.getSerialNumber()).isPresent()) {
            throw new RuntimeException("序号 " + card.getSerialNumber() + " 已存在");
        }
        
        // 设置默认值
        if (card.getUsageCount() == null) {
            card.setUsageCount(0);
        }
        if (card.getStatus() == null) {
            card.setStatus("可用");
        }
        if (card.getCreatedTime() == null) {
            card.setCreatedTime(LocalDateTime.now());
        }
        
        return ticketCardRepository.save(card);
    }
    
    // 更新票卡信息
    public TicketCard updateTicketCard(Long id, TicketCard cardDetails) {
        TicketCard card = getTicketCardById(id);
        
        // 检查是否可以更新（如果已经生成用户链接，某些字段不能修改）
        if (card.getUserUrl() != null && !card.getUserUrl().isEmpty()) {
            // 如果已经有用户链接，不允许修改序号和卡票URL
            if (!card.getSerialNumber().equals(cardDetails.getSerialNumber())) {
                throw new RuntimeException("已生成用户链接的票卡不能修改序号");
            }
            if (!card.getCardUrl().equals(cardDetails.getCardUrl())) {
                throw new RuntimeException("已生成用户链接的票卡不能修改卡票URL");
            }
        } else {
            // 检查序号是否重复（除了自己）
            Optional<TicketCard> existingCard = ticketCardRepository.findBySerialNumber(cardDetails.getSerialNumber());
            if (existingCard.isPresent() && !existingCard.get().getId().equals(id)) {
                throw new RuntimeException("序号 " + cardDetails.getSerialNumber() + " 已存在");
            }
            card.setSerialNumber(cardDetails.getSerialNumber());
            card.setCardUrl(cardDetails.getCardUrl());
        }
        
        // 更新其他字段
        card.setUsageCount(cardDetails.getUsageCount());
        card.setStatus(cardDetails.getStatus());
        
        return ticketCardRepository.save(card);
    }
    
    // 删除票卡
    public void deleteTicketCard(Long id) {
        TicketCard card = getTicketCardById(id);
        
        // 检查是否可以删除（如果已经生成用户链接，不能删除）
        if (card.getUserUrl() != null && !card.getUserUrl().isEmpty()) {
            throw new RuntimeException("已生成用户链接的票卡不能删除");
        }
        
        ticketCardRepository.deleteById(id);
    }
    
    // 根据code获取票卡信息
    public TicketCard getTicketCardByCode(String code) {
        return ticketCardRepository.findByGeneratedCode(code)
                .orElseThrow(() -> new RuntimeException("未找到对应的票卡"));
    }
    
    // 增加使用次数
    public TicketCard incrementUsageCount(Long id) {
        TicketCard card = getTicketCardById(id);
        card.setUsageCount(card.getUsageCount() + 1);
        
        // 如果达到一定次数，可以自动更新状态
        if (card.getUsageCount() >= 10) { // 假设10次为上限
            card.setStatus("已使用");
        }
        
        return ticketCardRepository.save(card);
    }
    
    // 重置票卡
    public TicketCard resetTicketCard(Long id) {
        TicketCard card = getTicketCardById(id);
        
        card.setUsageCount(0);
        card.setUserUrl(null);
        card.setGeneratedCode(null);
        card.setStatus("可用");
        
        return ticketCardRepository.save(card);
    }
    
    // 生成随机code
    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }
}
package com.grace.ticket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.grace.ticket.entity.VirtualCard;
import com.grace.ticket.repository.VirtualCardRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableScheduling
public class VirtualCardResetScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(VirtualCardResetScheduler.class);
    
    private final VirtualCardRepository virtualCardRepository;
    
    // 使用构造函数注入
    @Autowired
    public VirtualCardResetScheduler(VirtualCardRepository virtualCardRepository) {
        this.virtualCardRepository = virtualCardRepository;
    }
    
    /**
     * 每5分钟执行一次检查300000
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void resetVirtualCards() {
        log.info("开始执行虚拟乘车卡重置检查...");
        
        try {
            List<VirtualCard> cardsToReset = virtualCardRepository
                    .findByCardInitialStartTimeBefore(LocalDateTime.now().minusDays(1));
            
            if (cardsToReset.isEmpty()) {
                log.info("没有需要重置的虚拟乘车卡");
                return;
            }
            
            log.info("找到 {} 张需要重置的虚拟乘车卡", cardsToReset.size());
            
            for (VirtualCard card : cardsToReset) {
                log.info("重置虚拟乘车卡: {}, 初始时间: {}", 
                        card.getId(), card.getCardInitialStartTime());
                
                card.resetToInitialState();
                virtualCardRepository.save(card);
            }
            
            log.info("虚拟乘车卡重置完成，共重置 {} 张卡", cardsToReset.size());
            
        } catch (Exception e) {
            log.error("虚拟乘车卡重置任务执行失败", e);
        }
    }
    
    @Scheduled(cron = "0 0 6 * * ?")
    @Transactional
    public void dailyResetCheck() {
        log.info("开始执行每日虚拟乘车卡重置检查...");
        
        try {
            List<VirtualCard> cardsWithInitialTime = virtualCardRepository
                    .findByCardInitialStartTimeIsNotNull();
            
            int resetCount = 0;
            LocalDateTime now = LocalDateTime.now();
            
            for (VirtualCard card : cardsWithInitialTime) {
                LocalDateTime initialTime = card.getCardInitialStartTime();
                if (initialTime != null && now.isAfter(initialTime.plusDays(1))) {
                    log.info("每日检查重置虚拟乘车卡: {}, 初始时间: {}", 
                            card.getId(), initialTime);
                    
                    card.resetToInitialState();
                    virtualCardRepository.save(card);
                    resetCount++;
                }
            }
            
            log.info("每日虚拟乘车卡重置检查完成，重置 {} 张卡", resetCount);
            
        } catch (Exception e) {
            log.error("每日虚拟乘车卡重置检查失败", e);
        }
    }
}
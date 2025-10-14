package com.grace.ticket.service;

import com.grace.ticket.entity.VirtualCard;
import com.grace.ticket.repository.VirtualCardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Slf4j
@Component
public class VirtualCardTestData implements CommandLineRunner {
    
    private final VirtualCardRepository virtualCardRepository;
    
    // 使用构造函数注入
    @Autowired
    public VirtualCardTestData(VirtualCardRepository virtualCardRepository) {
        this.virtualCardRepository = virtualCardRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始创建虚拟乘车卡测试数据...");
        
        // 检查是否已存在测试数据，避免重复创建
        if (virtualCardRepository.findById("TEST_CARD_001").isPresent()) {
        	System.out.println("测试数据已存在，跳过创建");
            return;
        }
        
        // 创建测试数据
        VirtualCard testCard = new VirtualCard();
        testCard.setId("TEST_CARD_001");
        testCard.setStatus(1);
        testCard.setPeriodStatus(2); // 非初始状态
        testCard.setCurrentUsedBy("TEST_USER");
        testCard.setCurrentUsageStartTime(LocalDateTime.now().minusHours(2));
        testCard.setCardInitialStartTime(LocalDateTime.now().minusDays(2)); // 2天前，应该被重置
        
        testCard.setPhysicalCardAPhone("13800138000");
        testCard.setPhysicalCardAPwd("123456");
        testCard.setPhysicalCardBPhone("13900139000");
        testCard.setPhysicalCardBPwd("654321");
        testCard.setUsageRule("1;2;3;4;5;6;7");
        
        virtualCardRepository.save(testCard);
        System.out.println("测试虚拟乘车卡数据创建完成: {}" + testCard.getId());
    }
}
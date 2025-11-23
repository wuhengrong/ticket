package com.grace.ticket.service;



import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grace.ticket.controller.VipAdminController;
import com.grace.ticket.dto.GenerateRideLinkResponse;
import com.grace.ticket.dto.VipCardDTO;
import com.grace.ticket.dto.VipCustomerDTO;
import com.grace.ticket.entity.VipCard;
import com.grace.ticket.entity.VipCard.CardStatus;
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
    
    
    
    
    /**
     * 根据卡ID和乘车记录更新VIP卡信息
     */
    public VipCardDTO updateCardFromRideRecords(Long cardId, VipAdminController.RideRecordUpdateRequest updateRequest) {
        VipCard vipCard = vipCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("VIP卡不存在"));
        
        List<Map<String, String>> rideRecords = updateRequest.getRideRecords();
        
        if (rideRecords == null || rideRecords.isEmpty()) {
            throw new RuntimeException("没有乘车记录数据");
        }
        
        // 处理乘车记录逻辑
        processRideRecords(vipCard, rideRecords);
        
        VipCard savedCard = vipCardRepository.save(vipCard);
        return new VipCardDTO(savedCard);
    }
    
    /**
     * 根据手机号码和乘车记录更新VIP卡信息
     */
    public VipCardDTO updateCardFromRideRecordsByPhone(VipAdminController.RideRecordUpdateByPhoneRequest updateRequest) {
        String phoneNumber = updateRequest.getPhoneNumber();
        List<Map<String, String>> rideRecords = updateRequest.getRideRecords();
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new RuntimeException("手机号码不能为空");
        }
        
        if (rideRecords == null || rideRecords.isEmpty()) {
            throw new RuntimeException("没有乘车记录数据");
        }
        
        // 根据手机号码查找VIP卡
        VipCard vipCard = vipCardRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("未找到手机号码 " + phoneNumber + " 对应的VIP卡"));
        
        System.out.println("找到VIP卡: " + vipCard.getCardNumber() + "，状态: " + vipCard.getStatus());
        
        // 处理乘车记录逻辑
        processRideRecords(vipCard, rideRecords);
        
        VipCard savedCard = vipCardRepository.save(vipCard);
        return new VipCardDTO(savedCard);
    }
    
    /**
     * 处理乘车记录并更新VIP卡信息
     */
    private void processRideRecords(VipCard vipCard, List<Map<String, String>> rideRecords) {
        if (rideRecords.isEmpty()) {
            return; // 0条记录，不处理
        }
        
        // 获取第一条记录
        Map<String, String> firstRecord = rideRecords.get(0);
        String firstTime = firstRecord.get("time");
        String firstInOut = firstRecord.get("inOut");
        String firstStation = firstRecord.get("station");
        
        System.out.println("第一条记录 - 时间: " + firstTime + ", 进出: " + firstInOut + ", 站点: " + firstStation);
        
        // 设置首次使用时间（总是使用第一条记录的时间）
        if (firstTime != null && !firstTime.trim().isEmpty()) {
            LocalDateTime firstUseTime = parseDateTime(firstTime);
            vipCard.setFirstUseTime(firstUseTime);
            // 设置过期时间（首次使用时间+1天）
            vipCard.setExpiryTime(firstUseTime.plusDays(1));
            System.out.println("设置首次使用时间: " + firstUseTime);
        }
        
        if (rideRecords.size() == 1) {
            // 只有1条记录的情况
            handleSingleRecord(vipCard, firstRecord);
        } else {
            // 多条记录的情况
            handleMultipleRecords(vipCard, rideRecords);
        }
    }
    
    /**
     * 处理单条记录的情况
     */
    private void handleSingleRecord(VipCard vipCard, Map<String, String> record) {
        String time = record.get("time");
        String inOut = record.get("inOut");
        String station = record.get("station");
        
        if (time != null && !time.trim().isEmpty()) {
            LocalDateTime dateTime = parseDateTime(time);
            
            // 设置上车时间
           
            if ("进站".equalsIgnoreCase(inOut)) {
            	 vipCard.setBoardingTime(dateTime);
                 vipCard.setFirstUseTime(dateTime);
                 vipCard.setExpiryTime(dateTime.plusDays(1));
                vipCard.setInOutStatus(VipCard.InOutStatus.IN);
                vipCard.setBoardingStation(station);
                System.out.println("单条记录 - 进站，设置上车时间和站点");
            } else if ("出站".equalsIgnoreCase(inOut)) {
                vipCard.setInOutStatus(VipCard.InOutStatus.OUT);
                vipCard.setAlightingTime(dateTime);
                vipCard.setAlightingStation(station);
                System.out.println("单条记录 - 出站，设置下车时间和站点");
            }
        }
    }
    
    /**
     * 处理多条记录的情况
     */
    private void handleMultipleRecords(VipCard vipCard, List<Map<String, String>> rideRecords) {
        // 获取最后一条记录
        Map<String, String> lastRecord = rideRecords.get(rideRecords.size() - 1);
        String lastTime = lastRecord.get("time");
        String lastInOut = lastRecord.get("inOut");
        String lastStation = lastRecord.get("station");
        
        System.out.println("最后一条记录 - 时间: " + lastTime + ", 进出: " + lastInOut + ", 站点: " + lastStation);
        
        if (lastTime != null && !lastTime.trim().isEmpty()) {
            LocalDateTime lastDateTime = parseDateTime(lastTime);
            
            if ("进站".equalsIgnoreCase(lastInOut)) {
                // 最后一条是进站记录
                vipCard.setInOutStatus(VipCard.InOutStatus.IN);
                vipCard.setBoardingTime(lastDateTime);
                vipCard.setBoardingStation(lastStation);
                // 清空下车信息
                vipCard.setAlightingTime(null);
                vipCard.setAlightingStation(null);
                vipCard.setStatus(CardStatus.IN_USE);
                System.out.println("多条记录 - 最后是进站，设置上车信息");
            } else if ("出站".equalsIgnoreCase(lastInOut)) {
                // 最后一条是出站记录
                vipCard.setInOutStatus(VipCard.InOutStatus.OUT);
                vipCard.setAlightingTime(lastDateTime);
                vipCard.setAlightingStation(lastStation);
                vipCard.setStatus(CardStatus.AVAILABLE);
                
                Map<String, String> lastSecondRecord = rideRecords.get(rideRecords.size() - 2);
                String lastSecondTime = lastSecondRecord.get("time");
                String lastSecondStation = lastSecondRecord.get("station");
                vipCard.setBoardingStation(lastSecondStation);
                if (lastSecondTime != null && !lastSecondTime.trim().isEmpty()) {
                    LocalDateTime lastSecondDateTime = parseDateTime(lastSecondTime);
                    vipCard.setBoardingTime(lastSecondDateTime);
                }
                
                System.out.println("多条记录 - 最后是出站，设置下车信息");
            }
        }
    }
    
    
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
                System.err.println("日期时间字符串为空，使用当前时间");
                return LocalDateTime.now();
            }
            
            // 清理字符串
            String cleanedDateTimeStr = cleanDateTimeString(dateTimeStr);
            
            System.out.println("尝试解析日期时间: '" + cleanedDateTimeStr + "'");
            
            // 尝试多种日期时间格式
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDateTime result = LocalDateTime.parse(cleanedDateTimeStr, formatter);
                    System.out.println("✅ 使用格式 '" + formatter.toString() + "' 解析成功: " + result);
                    return result;
                } catch (Exception e) {
                    // 继续尝试下一个格式
                }
            }
            
            // 如果所有格式都失败，尝试手动解析
            return parseDateTimeManually(cleanedDateTimeStr);
            
        } catch (Exception e) {
            System.err.println("❌ 所有解析方法都失败: '" + dateTimeStr + "'");
            e.printStackTrace();
            return LocalDateTime.now();
        }
    }

    /**
     * 清理日期时间字符串
     */
    private String cleanDateTimeString(String dateTimeStr) {
        if (dateTimeStr == null) return "";
        
        // 移除所有不可见字符和特殊空白字符
        return dateTimeStr.trim()
                .replaceAll("[\\s\\u00A0\\u1680\\u2000-\\u200A\\u2028\\u2029\\u202F\\u205F\\u3000\\uFEFF]+", " ")
                .replaceAll("[\\u200B-\\u200D\\u2060-\\u206F\\uFEFF]", "")
                .trim();
    }

    /**
     * 手动解析日期时间（作为备用方案）
     */
    private LocalDateTime parseDateTimeManually(String dateTimeStr) {
        try {
            System.out.println("尝试手动解析: " + dateTimeStr);
            
            // 提取数字部分
            String numbersOnly = dateTimeStr.replaceAll("[^0-9]", "");
            System.out.println("纯数字: " + numbersOnly);
            
            if (numbersOnly.length() >= 14) { // yyyyMMddHHmmss
                int year = Integer.parseInt(numbersOnly.substring(0, 4));
                int month = Integer.parseInt(numbersOnly.substring(4, 6));
                int day = Integer.parseInt(numbersOnly.substring(6, 8));
                int hour = Integer.parseInt(numbersOnly.substring(8, 10));
                int minute = Integer.parseInt(numbersOnly.substring(10, 12));
                int second = Integer.parseInt(numbersOnly.substring(12, 14));
                
                return LocalDateTime.of(year, month, day, hour, minute, second);
            }
            
            throw new IllegalArgumentException("无法手动解析日期时间");
            
        } catch (Exception e) {
            System.err.println("手动解析也失败: " + e.getMessage());
            throw e;
        }
    }
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
            
            //更新之前的active record
            vipQrRecordService.batchUpdateExpiredQrRecords();
            
            // 检查是否已有活跃的二维码记录
            VipQrRecord existingRecord = vipQrRecordService.findActiveByCustomerId(customerId);
            if (existingRecord != null) {
                return GenerateRideLinkResponse.success(existingRecord.getQrUrl(), customer.getRideCount());
            }
            
            //如果是测试账号U1015，返回第一个QR链接
            if(customer.getUserName().equals("U1015")) {
            	VipQR qr = vipQRRepository.getReferenceById(1l);
            	return GenerateRideLinkResponse.success(qr.getCardUrl(), customer.getRideCount());
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
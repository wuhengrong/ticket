package com.grace.ticket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello World! 33";
    }
    
    @GetMapping("/")
    public String home() {
        return "Welcome to Spring Boot!";
    }
    
    // 获取二维码数据
    
    // 初始化：返回初始次数
    @GetMapping("/init")
    public Map<String, Object> init(@RequestParam String code) {
        Map<String, Object> res = new HashMap<>();
        res.put("count", 5); // 假设初始5次，可从DB查
        return res;
    }

    // 使用一次
    @PostMapping("/use")
    public ResponseEntity<String> use(@RequestParam String code) {
        // TODO: 数据库 count-1 并保存乘车记录
        return ResponseEntity.ok("OK");
    }

    // 获取二维码数据
    @GetMapping("/qrcode")
    public String getQRCode(@RequestParam String code) {
        // 返回一个二维码字符串 (实际可以是加密token)
        return "QR-" + code + "-" + System.currentTimeMillis();
    }

    // 可用票卡列表
    @GetMapping("/list")
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", "CARD123", "status", "正常", "available", true));
        list.add(Map.of("id", "CARD124", "status", "冻结", "available", false));
        return list;
    }

    // 乘车记录
    @GetMapping("/records")
    public List<Map<String, Object>> records(@RequestParam String code) {
        return List.of(
            Map.of("time", "2025-09-20 10:00", "detail", "乘车成功"),
            Map.of("time", "2025-09-21 09:30", "detail", "乘车成功")
        );
    }
}
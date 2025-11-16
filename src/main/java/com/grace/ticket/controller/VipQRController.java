package com.grace.ticket.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grace.ticket.entity.VipQR;
import com.grace.ticket.service.VipQRService;

@RestController
@RequestMapping("/api/admin/vip/qrs")
public class VipQRController {

    @Autowired
    private VipQRService vipQRService;

    @GetMapping
    public ResponseEntity<?> getAllVipQRs() {
        try {
            List<VipQR> qrs = vipQRService.getAllVipQRs();
            List<Map<String, Object>> result = qrs.stream().map(this::convertToMap).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVipQRById(@PathVariable Long id) {
        try {
            Optional<VipQR> vipQR = vipQRService.getVipQRById(id);
            if (vipQR.isPresent()) {
                return ResponseEntity.ok(convertToMap(vipQR.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createVipQR(@RequestBody VipQR vipQR) {
        try {
            VipQR createdQR = vipQRService.createVipQR(vipQR);
            return ResponseEntity.ok(convertToMap(createdQR));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVipQR(@PathVariable Long id, @RequestBody VipQR vipQRDetails) {
        try {
            VipQR updatedQR = vipQRService.updateVipQR(id, vipQRDetails);
            return ResponseEntity.ok(convertToMap(updatedQR));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVipQR(@PathVariable Long id) {
        try {
            boolean deleted = vipQRService.deleteVipQR(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchVipQRs(@RequestParam String keyword) {
        try {
            List<VipQR> qrs = vipQRService.searchVipQRs(keyword);
            List<Map<String, Object>> result = qrs.stream().map(this::convertToMap).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getVipQRStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", vipQRService.getTotalCount());
            stats.put("available", vipQRService.getCountByStatus(VipQR.QRStatus.AVAILABLE));
            stats.put("inUse", vipQRService.getCountByStatus(VipQR.QRStatus.IN_USE));
            stats.put("used", vipQRService.getCountByStatus(VipQR.QRStatus.USED));
            stats.put("expired", vipQRService.getCountByStatus(VipQR.QRStatus.EXPIRED));
            stats.put("disabled", vipQRService.getCountByStatus(VipQR.QRStatus.DISABLED));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> convertToMap(VipQR qr) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", qr.getId());
        map.put("cardUrl", qr.getCardUrl());
        map.put("rideCount", qr.getRideCount());
        map.put("status", qr.getStatus().name());
        map.put("creator", qr.getCreator());
        map.put("userName", qr.getUserName());
        map.put("usedTime", qr.getUsedTime());
        map.put("createdTime", qr.getCreatedTime());
        map.put("updatedTime", qr.getUpdatedTime());
        map.put("remark", qr.getRemark());
        return map;
    }
}
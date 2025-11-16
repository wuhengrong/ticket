package com.grace.ticket.controller;


import com.grace.ticket.entity.VipQrRecord;
import com.grace.ticket.service.VipQrRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/vip/qr-records")
public class VipQrRecordController {
    
    @Autowired
    private VipQrRecordService vipQrRecordService;
    
    @GetMapping
    public ResponseEntity<List<VipQrRecord>> getAllVipQrRecords() {
        try {
            List<VipQrRecord> qrRecords = vipQrRecordService.getAllVipQrRecords();
            return ResponseEntity.ok(qrRecords);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VipQrRecord> getVipQrRecordById(@PathVariable Long id) {
        try {
            Optional<VipQrRecord> qrRecord = vipQrRecordService.getVipQrRecordById(id);
            return qrRecord.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createVipQrRecord(@RequestBody VipQrRecord vipQrRecord) {
        try {
            VipQrRecord createdRecord = vipQrRecordService.createVipQrRecord(vipQrRecord);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "VIP二维码记录创建成功");
            response.put("data", createdRecord);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVipQrRecord(@PathVariable Long id, @RequestBody VipQrRecord vipQrRecordDetails) {
        try {
            VipQrRecord updatedRecord = vipQrRecordService.updateVipQrRecord(id, vipQrRecordDetails);
            if (updatedRecord != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "VIP二维码记录更新成功");
                response.put("data", updatedRecord);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "记录不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVipQrRecord(@PathVariable Long id) {
        try {
            boolean deleted = vipQrRecordService.deleteVipQrRecord(id);
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "VIP二维码记录删除成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "记录不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<VipQrRecord>> searchVipQrRecords(@RequestParam String keyword) {
        try {
            List<VipQrRecord> qrRecords = vipQrRecordService.searchVipQrRecords(keyword);
            return ResponseEntity.ok(qrRecords);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getVipQrRecordStats() {
        try {
            Map<String, Object> stats = vipQrRecordService.getVipQrRecordStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<VipQrRecord> getActiveQrRecordByCustomerId(@PathVariable Long customerId) {
        try {
            Optional<VipQrRecord> qrRecord = vipQrRecordService.getActiveQrRecordByCustomerId(customerId);
            return qrRecord.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
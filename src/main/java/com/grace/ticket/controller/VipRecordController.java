// VipRecordController.java
package com.grace.ticket.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grace.ticket.dto.VipRecordDTO;
import com.grace.ticket.entity.VipRecord;
import com.grace.ticket.service.VipRecordService;

@RestController
@RequestMapping("/api/admin/vip/records")
@CrossOrigin(origins = "*")
public class VipRecordController {
    
    @Autowired
    private VipRecordService vipRecordService;
    
    @GetMapping
    public ResponseEntity<List<VipRecordDTO>> getAllVipRecords() {
        List<VipRecordDTO> records = vipRecordService.getAllVipRecords();
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VipRecordDTO> getVipRecordById(@PathVariable Long id) {
        VipRecordDTO record = vipRecordService.getVipRecordById(id);
        return ResponseEntity.ok(record);
    }
    
    @PostMapping
    public ResponseEntity<VipRecordDTO> createVipRecord(@RequestBody VipRecord record) {
        VipRecordDTO createdRecord = vipRecordService.createVipRecord(record);
        return ResponseEntity.ok(createdRecord);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<VipRecordDTO> updateVipRecord(@PathVariable Long id, @RequestBody VipRecord record) {
        VipRecordDTO updatedRecord = vipRecordService.updateVipRecord(id, record);
        return ResponseEntity.ok(updatedRecord);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVipRecord(@PathVariable Long id) {
        vipRecordService.deleteVipRecord(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<VipRecordDTO>> searchVipRecords(@RequestParam String keyword) {
        List<VipRecordDTO> records = vipRecordService.searchVipRecords(keyword);
        return ResponseEntity.ok(records);
    }
}
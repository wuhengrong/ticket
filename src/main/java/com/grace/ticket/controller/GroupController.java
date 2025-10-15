// GroupController.java
package com.grace.ticket.controller;

import com.grace.ticket.dto.ApiResponse;
import com.grace.ticket.entity.Group;
import com.grace.ticket.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/groups")
@CrossOrigin(origins = "*")
public class GroupController {
    
    @Autowired
    private GroupService groupService;
    
    // 获取所有分组
    @GetMapping
    public ApiResponse<List<Group>> getAllGroups() {
        try {
            List<Group> groups = groupService.findAll();
            return ApiResponse.success(groups);
        } catch (Exception e) {
            return ApiResponse.error("获取分组列表失败: " + e.getMessage());
        }
    }
    
    // 根据ID获取分组
    @GetMapping("/{groupId}")
    public ApiResponse<Group> getGroupById(@PathVariable String groupId) {
        try {
            Optional<Group> group = groupService.findById(groupId);
            if (group.isPresent()) {
                return ApiResponse.success(group.get());
            } else {
                return ApiResponse.error("分组不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("获取分组失败: " + e.getMessage());
        }
    }
    
    // 创建分组
    @PostMapping
    public ApiResponse<Group> createGroup(@RequestBody Group group) {
        try {
            // 检查分组ID是否已存在
            if (groupService.findById(group.getGroupId()).isPresent()) {
                return ApiResponse.error("分组ID已存在");
            }
            
            Group savedGroup = groupService.save(group);
            return ApiResponse.success(savedGroup, "分组创建成功");
        } catch (Exception e) {
            return ApiResponse.error("创建分组失败: " + e.getMessage());
        }
    }
    
    // 更新分组
    @PutMapping("/{groupId}")
    public ApiResponse<Group> updateGroup(@PathVariable String groupId, @RequestBody Group group) {
        try {
            // 检查分组是否存在
            if (!groupService.findById(groupId).isPresent()) {
                return ApiResponse.error("分组不存在");
            }
            
            group.setGroupId(groupId); // 确保ID一致
            Group updatedGroup = groupService.save(group);
            return ApiResponse.success(updatedGroup, "分组更新成功");
        } catch (Exception e) {
            return ApiResponse.error("更新分组失败: " + e.getMessage());
        }
    }
    
    // 删除分组
    @DeleteMapping("/{groupId}")
    public ApiResponse<Void> deleteGroup(@PathVariable String groupId) {
        try {
            if (!groupService.findById(groupId).isPresent()) {
                return ApiResponse.error("分组不存在");
            }
            
            groupService.deleteById(groupId);
            return ApiResponse.success(null, "分组删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除分组失败: " + e.getMessage());
        }
    }
}
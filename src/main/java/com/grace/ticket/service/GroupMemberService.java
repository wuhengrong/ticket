// GroupMemberService.java
package com.grace.ticket.service;

import com.grace.ticket.entity.GroupMember;
import com.grace.ticket.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupMemberService {
    
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    
    public List<GroupMember> findAll() {
        return groupMemberRepository.findAll();
    }
    
    public List<GroupMember> findByGroupId(String groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }
    
    public Optional<GroupMember> findByGroupIdAndUserId(String groupId, String userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
    }
    
    public GroupMember save(GroupMember groupMember) {
        return groupMemberRepository.save(groupMember);
    }
    
    public void deleteByGroupIdAndUserId(String groupId, String userId) {
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }
    
 // 在 GroupMemberService 中添加
    public Optional<GroupMember> findByAccessCode(String accessCode) {
        return groupMemberRepository.findByAccessCode(accessCode);
    }
}
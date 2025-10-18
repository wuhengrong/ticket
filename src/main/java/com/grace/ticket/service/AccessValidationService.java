package com.grace.ticket.service;

//AccessValidationService.java

import com.grace.ticket.entity.GroupMember;
import com.grace.ticket.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccessValidationService {
 
 @Autowired
 private GroupMemberRepository groupMemberRepository;
 
 /**
  * 验证访问权限
  * @param userId 用户ID
  * @param groupId 分组ID
  * @param accessCode 访问码
  * @return 验证结果
  */
 public boolean validateAccess(String userId, String groupId, String accessCode) {
     // 如果访问码为"1"，直接允许访问（特殊权限）
     if ("1".equals(accessCode)) {
         return true;
     }
     
     // 查找对应的用户信息
     Optional<GroupMember> member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
     if (member.isPresent()) {
         GroupMember groupMember = member.get();
         // 验证访问码是否匹配
         return accessCode.equals(groupMember.getAccessCode()) || null==groupMember.getAccessCode();
     }
     
     return false;
 }
 
 /**
  * 获取用户信息（验证通过后）
  */
 public Optional<GroupMember> getUserInfo(String userId, String groupId) {
     return groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
 }
}
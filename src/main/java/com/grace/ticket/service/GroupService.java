package com.grace.ticket.service;


import com.grace.ticket.entity.Group;
import com.grace.ticket.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupService {
 
 @Autowired
 private GroupRepository groupRepository;
 
 public List<Group> findAll() {
     return groupRepository.findAll();
 }
 
 public Optional<Group> findById(String groupId) {
     return groupRepository.findById(groupId);
 }
 
 public Group save(Group group) {
     return groupRepository.save(group);
 }
 
 public void deleteById(String groupId) {
     groupRepository.deleteById(groupId);
 }
}
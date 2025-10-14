package com.grace.ticket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grace.ticket.entity.Group;

//GroupRepository.java
@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
 
 Optional<Group> findByGroupId(String groupId);
 
 List<Group> findByStatus(String status);
}
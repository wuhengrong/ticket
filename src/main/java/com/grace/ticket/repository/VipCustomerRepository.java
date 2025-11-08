package com.grace.ticket.repository;

import com.grace.ticket.entity.VipCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VipCustomerRepository extends JpaRepository<VipCustomer, Long> {
    
    Optional<VipCustomer> findByVipUrl(String vipUrl);
    
    @Modifying
    @Query("UPDATE VipCustomer vc SET vc.rideCount = vc.rideCount - 1 WHERE vc.id = :customerId AND vc.rideCount > 0")
    int decrementRideCount(@Param("customerId") Long customerId);
    
    
    Optional<VipCustomer> findByUserName(String userName);
}
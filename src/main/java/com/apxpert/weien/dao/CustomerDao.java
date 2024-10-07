package com.apxpert.weien.dao;

import com.apxpert.weien.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerDao extends JpaRepository<Customer, Integer> {
    Customer findByUsername(String username);
}
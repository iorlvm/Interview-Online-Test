package com.apxpert.weien.dao;

import com.apxpert.weien.entity.OrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderMasterDao extends JpaRepository<OrderMaster, Integer> {
}
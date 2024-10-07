package com.apxpert.weien.dao;

import com.apxpert.weien.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailDao extends JpaRepository<OrderDetail, Integer> {
}
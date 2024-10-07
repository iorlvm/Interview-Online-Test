package com.apxpert.weien.service;

import com.apxpert.weien.dto.OrderDTO;
import org.springframework.transaction.annotation.Transactional;


public interface OrderService {
    @Transactional
    OrderDTO createOrder(OrderDTO orderDTO);
}

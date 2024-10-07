package com.apxpert.weien.service;

import com.apxpert.weien.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface OrderService {
    @Transactional
    OrderDTO createOrder(OrderDTO orderDTO);

    @Transactional(readOnly = true)
    Page<OrderDTO> getOrderList(Integer page, Integer size);

    Boolean validOrderRequest (OrderDTO orderDTO);
}

package com.apxpert.weien.controller;

import com.apxpert.weien.dto.OrderDTO;
import com.apxpert.weien.dto.Result;
import com.apxpert.weien.entity.OrderDetail;
import com.apxpert.weien.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("orders")
    public Result createOrder(@RequestBody OrderDTO orderDTO) {
        OrderDTO order = orderService.createOrder(orderDTO);
        return Result.ok(order);
    }
}

package com.apxpert.weien.service.impl;

import com.apxpert.weien.dao.CustomerDao;
import com.apxpert.weien.entity.Customer;
import com.apxpert.weien.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MemberServiceImpl implements MemberService {
    @Autowired
    private CustomerDao customerDao;

    @Override
    public Customer autoGenerated(Customer customer) {
        if (customer == null || customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("未輸入訂購人");
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        customer.setUsername(uuid);
        customer.setPassword("");
        customer.setAutoGenerated(true);

        return customerDao.save(customer);
    }
}

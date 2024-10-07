package com.apxpert.weien.service;

import com.apxpert.weien.entity.Customer;

public interface TokenService {
    String createToken(Customer user);

    Customer validateToken(String token);

    void revokeToken(String token);

    void flashLoginExpire(String token);
}
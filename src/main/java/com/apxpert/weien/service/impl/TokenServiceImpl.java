package com.apxpert.weien.service.impl;

import com.apxpert.weien.entity.Customer;
import com.apxpert.weien.service.TokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenServiceImpl implements TokenService {
    private static final String LOGIN_USER = "login:";
    private static final Long LOGIN_TTL = 3600L;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public String createToken(Customer user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = LOGIN_USER + token;

        String json = null;
        try {
            json = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        stringRedisTemplate.opsForValue().set(key, json, LOGIN_TTL, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public Customer validateToken(String token) {
        String key = LOGIN_USER + token;
        String json = stringRedisTemplate.opsForValue().get(key);


        if (json != null) {
            try {
                return objectMapper.readValue(json, Customer.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    @Override
    public void revokeToken(String token) {
        String key = LOGIN_USER + token;
        stringRedisTemplate.delete(key);
    }

    @Override
    public void flashLoginExpire(String token) {
        String key = LOGIN_USER + token;
        stringRedisTemplate.expire(key, LOGIN_TTL, TimeUnit.SECONDS);
    }
}

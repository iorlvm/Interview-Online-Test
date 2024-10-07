package com.apxpert.weien.service.impl;

import com.apxpert.weien.dao.ProductDao;
import com.apxpert.weien.entity.Product;
import com.apxpert.weien.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductDao productDao;

    @Override
    public Page<Product> findAll(Integer page, Integer size) {
        return  productDao.findAll(PageRequest.of(page, size));
    }
}

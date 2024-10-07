package com.apxpert.weien.service;

import com.apxpert.weien.entity.Product;
import org.springframework.data.domain.Page;

public interface ProductService {
    Page<Product> findAll(Integer page, Integer size);

    Product findById(Integer productId);
    
    Product update(Product product);

    void deleteById(Integer productId);

    Product appProduct(Product product);
}

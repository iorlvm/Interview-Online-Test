package com.apxpert.weien.dao;

import com.apxpert.weien.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Map;

public interface ProductDao extends JpaRepository<Product, Integer>, CustomProductDao {
}
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
        return productDao.findAll(PageRequest.of(page, size));
    }

    @Override
    public Product findById(Integer productId) {
        return productDao.findById(productId).orElse(null);
    }

    @Override
    public Product update(Product product) {
        Product productFromDb = productDao.findById(product.getId()).orElseThrow(() -> new IllegalArgumentException("參數錯誤: 不存在的商品"));

        // 更新各屬性
        if (product.getName() != null) {
            productFromDb.setName(product.getName());
        }
        if (product.getPrice() != null) {
            if (product.getPrice() < 0) throw new IllegalArgumentException("價格不得為負數");
            productFromDb.setPrice(product.getPrice());
        }
        if (product.getStock() != null) {
            if (product.getStock() < 0) throw new IllegalArgumentException("庫存不得為負數");
            productFromDb.setStock(product.getStock());
        }
        if (product.getUnit() != null) {
            productFromDb.setUnit(product.getUnit());
        }
        if (product.getImgSrc() != null) {
            productFromDb.setImgSrc(product.getImgSrc());
        }

        // 將更新後的商品實體保存回資料庫
        return productDao.save(productFromDb);
    }

    @Override
    public void deleteById(Integer productId) {
        Product product = productDao.findById(productId).orElseThrow(() -> new IllegalArgumentException("參數錯誤: 不存在的商品"));
        productDao.delete(product);
    }

    @Override
    public Product appProduct(Product product) {
        // 檢查各個參數
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("產品名稱必須提供");
        }

        if (product.getPrice() == null || product.getPrice() < 0) {
            throw new IllegalArgumentException("產品價格必須為非負數");
        }

        if (product.getStock() == null || product.getStock() < 0) {
            throw new IllegalArgumentException("產品庫存必須為非負數");
        }

        if (product.getUnit() == null || product.getUnit().trim().isEmpty()) {
            product.setUnit("個"); // 未設定時給予預設值
        }

        if (product.getImgSrc() == null || product.getImgSrc().trim().isEmpty()) {
            product.setImgSrc("http://fakeimg.pl/150x150"); // 未設定時給予預設值
        }

        return productDao.save(product);
    }
}

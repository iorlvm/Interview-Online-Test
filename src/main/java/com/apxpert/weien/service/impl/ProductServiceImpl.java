package com.apxpert.weien.service.impl;

import com.apxpert.weien.dao.ProductDao;
import com.apxpert.weien.entity.Product;
import com.apxpert.weien.service.ProductService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {
    private final static String PRODUCT_STOCK = "product:stock";

    @Autowired
    ProductDao productDao;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @PostConstruct
    public void loadProductStockToRedis() {
        // TODO: 將MQ中的隊列在啟動時完整處理並寫入RDB

        // 預先將商品庫存載入redis中
        List<Product> products = productDao.findAll();

        // 將商品ID和庫存以 Hash 形式存入 Redis
        for (Product product : products) {
            stringRedisTemplate.opsForHash().put(PRODUCT_STOCK,
                    String.valueOf(product.getId()),
                    String.valueOf(product.getStock()));
        }
    }


    @Override
    public Page<Product> findAll(Integer page, Integer size) {
        Page<Product> products = productDao.findAll(PageRequest.of(page, size));
        // TODO: 從redis中讀取最新的庫存狀態
        return products;
    }

    @Override
    public Product findById(Integer productId) {
        Product product = productDao.findById(productId).orElse(null);
        if (product != null) {
            // TODO: 從redis中讀取最新的庫存狀態
        }
        return product;
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
            // TODO: 修改為更新redis中的庫存 再將庫存更新放到MQ的尾端, 這個時間點不更新
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
        // TODO: 同時刪除redis中的資料
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

        Product saved = productDao.save(product);
        // TODO: 新增庫存到redis中
        return saved;
    }
}

package com.apxpert.weien.service.impl;

import com.apxpert.weien.dao.ProductDao;
import com.apxpert.weien.entity.Product;
import com.apxpert.weien.service.ProductService;
import com.apxpert.weien.service.ProductStockQueueService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.apxpert.weien.utils.Constant.PRODUCT_STOCK;


@Service
public class ProductServiceImpl implements ProductService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductDao productDao;
    @Autowired
    private ProductStockQueueService productStockQueueService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @PostConstruct
    public void loadProductStockToRedis() {
        // 將MQ中的隊列在啟動時完整處理並寫入RDB
        productStockQueueService.processQueueAndLoadToRDB();

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
        // 從 Redis 中讀取最新的庫存狀態
        products.getContent().forEach(product -> {
            String stock = (String) stringRedisTemplate.opsForHash().get(PRODUCT_STOCK, String.valueOf(product.getId()));
            if (stock != null) {
                // 避免查詢時對RDB更新庫存 (其實我不是很確定ORM自動更新的觸發條件, 但斷開比較安全)
                entityManager.detach(product);
                product.setStock(Integer.parseInt(stock));
            }
        });
        return products;
    }

    @Override
    public Product findById(Integer productId) {
        Product product = productDao.findById(productId).orElse(null);
        if (product != null) {
            String stock = (String) stringRedisTemplate.opsForHash().get(PRODUCT_STOCK, String.valueOf(product.getId()));
            if (stock != null) {
                // 避免查詢時對RDB更新庫存 (其實我不是很確定ORM自動更新的觸發條件, 但斷開比較安全)
                entityManager.detach(product);
                product.setStock(Integer.parseInt(stock));
            }
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

            // 更新庫存至 Redis
            stringRedisTemplate.opsForHash().put(PRODUCT_STOCK, String.valueOf(product.getId()), String.valueOf(product.getStock()));

            // 發送更新庫存到 MQ
            productStockQueueService.sendStockUpdate(product.getId(), product.getStock());
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

        // 刪除 Redis 中的商品庫存
        stringRedisTemplate.opsForHash().delete(PRODUCT_STOCK, String.valueOf(productId));
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

        // 新增庫存到 Redis 中
        stringRedisTemplate.opsForHash().put(PRODUCT_STOCK, String.valueOf(saved.getId()), String.valueOf(saved.getStock()));

        return saved;
    }
}

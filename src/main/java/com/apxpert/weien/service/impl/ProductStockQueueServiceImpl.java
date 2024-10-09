package com.apxpert.weien.service.impl;

import com.apxpert.weien.dao.ProductDao;
import com.apxpert.weien.entity.Product;
import com.apxpert.weien.service.ProductStockQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ProductStockQueueServiceImpl implements ProductStockQueueService {
    private final static String QUEUE_NAME = "stream.product.stock";
    private final static String STOCK_GROUP = "stock_group";
    private final static int RETRY_TIMES = 20;
    private final static String CLIENT = "c1";

    private final static Logger log = LoggerFactory.getLogger(ProductStockQueueServiceImpl.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductDao productDao;

    private static final ExecutorService PRODUCT_STOCK_EXECUTOR = Executors.newSingleThreadExecutor();

    @Override
    public void processQueueAndLoadToRDB() {
        // 處理所有未處理的MQ
        while (true) {
            try {
                // 從隊列中取出一筆資料
                List<MapRecord<String, Object, Object>> read = stringRedisTemplate.opsForStream().read(
                        Consumer.from(STOCK_GROUP, CLIENT),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create(QUEUE_NAME, ReadOffset.lastConsumed())
                );

                // 檢查隊列中是否有資料 (無資料: 中止)
                if (read == null || read.isEmpty()) {
                    break;
                }

                // 有資料解析資料, 並更新資料庫中的庫存
                MapRecord<String, Object, Object> record = read.get(0);
                processProductStockQueue(record);

                // 處理完畢 (標記為已處理)
                stringRedisTemplate.opsForStream().acknowledge(QUEUE_NAME, STOCK_GROUP, record.getId());
            } catch (Exception e) {
                // 異常重試
                handlePendingList();
            }
        }

        // 處理完畢後, 啟動隊列監聽處理
        PRODUCT_STOCK_EXECUTOR.submit(() -> {
            while (true) {
                try {
                    // 從隊列中取出一筆資料
                    List<MapRecord<String, Object, Object>> read = stringRedisTemplate.opsForStream().read(
                            Consumer.from(STOCK_GROUP, CLIENT),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(QUEUE_NAME, ReadOffset.lastConsumed())
                    );

                    // 檢查隊列中是否有資料 (無資料: 繼續監聽)
                    if (read == null || read.isEmpty()) {
                        continue;
                    }

                    // 有資料解析資料, 並更新資料庫中的庫存
                    MapRecord<String, Object, Object> record = read.get(0);
                    processProductStockQueue(record);

                    // 處理完畢 (標記為已處理)
                    stringRedisTemplate.opsForStream().acknowledge(QUEUE_NAME, STOCK_GROUP, record.getId());
                } catch (Exception e) {
                    // 異常重試
                    handlePendingList();
                }
            }
        });
    }

    @Override
    public void sendStockUpdate(Integer productId, Integer stock) {
        // 構建要發送到消息隊列的數據
        Map<String, Object> message = Map.of(
                "productId", productId.toString(),
                "newStock", stock
        );

        // 發送消息到MQ中
        stringRedisTemplate.opsForStream().add(QUEUE_NAME, message);
    }

    private void processProductStockQueue(MapRecord<String, Object, Object> record) {
        Map<Object, Object> value = record.getValue();

        String productIdStr = (String) value.get("productId");
        Integer newStock = (Integer) value.get("newStock");

        if (productIdStr != null && newStock != null) {
            // 更新商品庫存
            Product product = productDao.findById(Integer.parseInt(productIdStr)).orElse(null);
            if (product != null) {
                product.setStock(newStock);
                productDao.save(product); // 更新資料庫中的庫存
            }
        }
    }

    private void handlePendingList() {
        MapRecord<String, Object, Object> record = null;
        for (int i = 0; i < RETRY_TIMES; i++) {
            try {
                // 從隊列中取出一筆資料
                List<MapRecord<String, Object, Object>> read = stringRedisTemplate.opsForStream().read(
                        Consumer.from(STOCK_GROUP, CLIENT),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(QUEUE_NAME, ReadOffset.from("0"))
                );

                // 檢查隊列中是否有資料
                if (read == null || read.isEmpty()) {
                    // 沒有異常訊息, 結束異常處理程序
                    return;
                }

                // 有資料解析資料, 並更新資料庫中的庫存
                record = read.get(0);
                processProductStockQueue(record);

                // 處理完畢 (標記為已處理)
                stringRedisTemplate.opsForStream().acknowledge(QUEUE_NAME, STOCK_GROUP, record.getId());
                return;  // 處理完成 結束
            } catch (Exception e) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (record != null) {
            // 超出重試次數
            log.error("Error processing record: {}", record.getId());
            // TODO: 應該要考慮增加一個異常隊列把卡死的部分丟去那邊處理
            stringRedisTemplate.opsForStream().acknowledge(QUEUE_NAME, STOCK_GROUP, record.getId());
        }
    }
}

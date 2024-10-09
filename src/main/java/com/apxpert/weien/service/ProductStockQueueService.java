package com.apxpert.weien.service;

public interface ProductStockQueueService {
    void processQueueAndLoadToRDB();
    void sendStockUpdate(Integer productId, Integer stock);
}

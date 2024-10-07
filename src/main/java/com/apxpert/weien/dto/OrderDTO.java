package com.apxpert.weien.dto;

import com.apxpert.weien.entity.Customer;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderDTO {
    private Integer orderId;
    private Integer customerId; // 使用者編號
    private Integer totalPrice; // 總價 (交易快照)
    private Date createDate; // 成交時間
    private Boolean success; // 成功與否

    private Customer customer; // 自動生成的訂購人帳號

    private List <Product> products;

    @Data
    public static class Product {
        private Integer count;

        // 訂單請求
        private Integer productId;

        // 訂單回應
        private String productName; // 商品名稱 (快照)
        private Integer productPrice; // 商品價格 (快照)
        private String productUnit; // 商品單位 (快照)
    }
}

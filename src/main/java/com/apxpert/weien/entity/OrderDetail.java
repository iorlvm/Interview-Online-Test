package com.apxpert.weien.entity;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "order_detail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; // 訂單細項編號

    @Column(name = "order_id")
    private Integer orderId; // 訂單編號 (FK)

    @Column(name = "count")
    private Integer count; // 數量

    @Column(name = "product_name")
    private String productName; // 商品名稱 (快照)

    @Column(name = "product_price")
    private Integer productPrice; // 商品價格 (快照)

    @Column(name = "product_unit")
    private String productUnit; // 商品單位 (快照)
}
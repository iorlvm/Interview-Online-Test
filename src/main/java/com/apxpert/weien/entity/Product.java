package com.apxpert.weien.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; // 商品編號

    @Column(name = "name")
    private String name; // 商品名稱

    @Column(name = "price")
    private Integer price; // 商品價格

    @Column(name = "stock")
    private Integer stock; // 庫存

    @Column(name = "unit")
    private String unit; // 單位

    @Column(name = "img_src")
    private String imgSrc; // 商品圖片網址

    @PrePersist
    protected void onCreate() {
        if (unit == null) unit = "個";
    }
}
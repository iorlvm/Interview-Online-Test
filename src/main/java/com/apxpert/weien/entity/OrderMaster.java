package com.apxpert.weien.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "order_master")
public class OrderMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; // 訂單編號

    @Column(name = "customer_id")
    private Integer customerId; // 使用者編號 (FK)

    @Column(name = "total_price")
    private Integer totalPrice; // 總價 (交易快照)

    @Column(name = "success")
    private Boolean success; // 交易成功 (y/n)

    @Column(name = "create_date", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate; // 成交時間

    @PrePersist
    protected void onCreate() {
        if (createDate == null) createDate = new Date();
    }
}

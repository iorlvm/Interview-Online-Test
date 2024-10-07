package com.apxpert.weien.service.impl;

import com.apxpert.weien.dao.OrderDetailDao;
import com.apxpert.weien.dao.OrderMasterDao;
import com.apxpert.weien.dao.ProductDao;
import com.apxpert.weien.dto.OrderDTO;
import com.apxpert.weien.entity.OrderDetail;
import com.apxpert.weien.entity.OrderMaster;
import com.apxpert.weien.entity.Product;
import com.apxpert.weien.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ProductDao productDao;
    @Autowired
    private OrderMasterDao orderMasterDao;
    @Autowired
    private OrderDetailDao orderDetailDao;

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        List<OrderDTO.Product> products = orderDTO.getProducts();

        if (products == null) {
            throw new IllegalArgumentException("參數錯誤: 無傳遞任何商品");
        }

        List<OrderDetail> temp = new ArrayList<>(products.size());
        for (OrderDTO.Product product : products) {
            OrderDetail orderDetail = new OrderDetail();
            Product productFromDb = productDao.findById(product.getProductId()).orElseThrow(() -> new IllegalArgumentException("參數錯誤: 不存在的商品"));

            // 檢查庫存
            if (productFromDb.getStock() < product.getCount()) {
                throw new IllegalArgumentException(
                        "庫存不足: " + productFromDb.getName() +
                        " 目前庫存僅剩 " + productFromDb.getStock() + " " + productFromDb.getUnit());
            }

            // 扣減庫存
            int stock = productFromDb.getStock() - product.getCount();
            productFromDb.setStock(stock);
            productDao.save(productFromDb);

            // 設定數量
            orderDetail.setCount(product.getCount());
            // 寫入快照屬性
            orderDetail.setProductName(productFromDb.getName());
            orderDetail.setProductUnit(productFromDb.getUnit());
            orderDetail.setProductPrice(productFromDb.getPrice());

            temp.add(orderDetail);
        }

        Integer totalPrice = calTotalPrice(temp);

        OrderMaster orderMaster = new OrderMaster();
        orderMaster.setSuccess(true);
        orderMaster.setCustomerId(1); // TODO: 未來擴充
        orderMaster.setTotalPrice(totalPrice);

        Integer orderId = orderMasterDao.save(orderMaster).getId();

        for (OrderDetail orderDetail : temp) {
            orderDetail.setOrderId(orderId);
        }

        List<OrderDetail> orderDetails = orderDetailDao.saveAll(temp);

        return convertToOrderDTO(orderMaster, orderDetails);
    }

    private OrderDTO convertToOrderDTO(OrderMaster orderMaster, List<OrderDetail> orderDetails) {
        OrderDTO res = new OrderDTO();
        BeanUtils.copyProperties(orderMaster, res);
        res.setOrderId(orderMaster.getId());

        List<OrderDTO.Product> products = new ArrayList<>(orderDetails.size());
        for (OrderDetail orderDetail : orderDetails) {
            OrderDTO.Product product = new OrderDTO.Product();
            BeanUtils.copyProperties(orderDetail, product);

            products.add(product);
        }
        res.setProducts(products);

        return res;
    }

    private Integer calTotalPrice(List<OrderDetail> orderDetails) {
        int totalPrice = 0;

        for (OrderDetail orderDetail : orderDetails) {
            totalPrice += orderDetail.getCount() * orderDetail.getProductPrice();
        }

        return totalPrice;
    }
}

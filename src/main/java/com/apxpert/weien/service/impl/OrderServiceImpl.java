package com.apxpert.weien.service.impl;

import com.apxpert.weien.dao.CustomerDao;
import com.apxpert.weien.dao.OrderDetailDao;
import com.apxpert.weien.dao.OrderMasterDao;
import com.apxpert.weien.dao.ProductDao;
import com.apxpert.weien.dto.OrderDTO;
import com.apxpert.weien.entity.Customer;
import com.apxpert.weien.entity.OrderDetail;
import com.apxpert.weien.entity.OrderMaster;
import com.apxpert.weien.entity.Product;
import com.apxpert.weien.service.OrderService;
import com.apxpert.weien.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private CustomerDao customerDao;
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

        Map<Integer, Integer> productMap = convertListToMap(products);
        Map<Integer, Integer> productStocksByIds = productDao.findProductStocksByIds(productMap.keySet());

        // 檢查庫存
        boolean success = true;
        for (Map.Entry<Integer, Integer> entry : productMap.entrySet()) {
            Integer productId = entry.getKey();
            Integer requestedCount = entry.getValue();

            // 獲取可用庫存
            Integer availableStock = productStocksByIds.get(productId);

            // 檢查庫存是否足夠
            if (availableStock == null || availableStock < requestedCount) {
                success = false;
                break;
            }
        }


        List<OrderDetail> temp = new ArrayList<>(productMap.size());
        for (Map.Entry<Integer, Integer> entry : productMap.entrySet()) {
            Integer productId = entry.getKey();
            Integer count = entry.getValue();

            OrderDetail orderDetail = new OrderDetail();
            Product productFromDb = productDao.findById(productId).orElseThrow(() -> new IllegalArgumentException("參數錯誤: 不存在的商品"));

            // 扣減庫存
            if (success) {
                int stock = productFromDb.getStock() - count;
                productFromDb.setStock(stock);
                productDao.save(productFromDb);
            }

            // 設定數量
            orderDetail.setCount(count);
            // 寫入快照屬性
            orderDetail.setProductName(productFromDb.getName());
            orderDetail.setProductUnit(productFromDb.getUnit());
            orderDetail.setProductPrice(productFromDb.getPrice());

            temp.add(orderDetail);
        }

        Integer totalPrice = calTotalPrice(temp);

        OrderMaster orderMaster = new OrderMaster();
        orderMaster.setSuccess(success);
        orderMaster.setCustomerId(UserHolder.getId());
        orderMaster.setTotalPrice(totalPrice);

        Integer orderId = orderMasterDao.save(orderMaster).getId();

        for (OrderDetail orderDetail : temp) {
            orderDetail.setOrderId(orderId);
        }

        List<OrderDetail> orderDetails = orderDetailDao.saveAll(temp);

        return convertToOrderDTO(orderMaster, orderDetails);
    }

    @Override
    public Page<OrderDTO> getOrderList(Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<OrderMaster> all = orderMasterDao.findAll(pageRequest);

        List<OrderDTO> orderDTOS = new ArrayList<>(all.getContent().size());
        for (OrderMaster orderMaster : all) {
            Integer orderId = orderMaster.getId();
            List<OrderDetail> byOrderId = orderDetailDao.findByOrderId(orderId);
            OrderDTO orderDTO = convertToOrderDTO(orderMaster, byOrderId);

            Integer customerId = orderMaster.getCustomerId();
            Customer customer = customerDao.findById(customerId).orElseThrow();
            customer.setPassword(null);
            orderDTO.setCustomer(customer);

            orderDTOS.add(orderDTO);
        }

        return new PageImpl<>(orderDTOS, pageRequest, all.getTotalElements());
    }

    @Override
    public Boolean validOrderRequest(OrderDTO orderDTO) {
        if (orderDTO == null || orderDTO.getProducts() == null) {
            throw new IllegalArgumentException("未傳入任何商品");
        }

        for (OrderDTO.Product product : orderDTO.getProducts()) {
            if (product.getCount() > 10) {
                throw new IllegalArgumentException("每種商品一次最多訂購10個");
            }
        }
        return true;
    }

    private OrderDTO convertToOrderDTO(OrderMaster orderMaster, List<OrderDetail> orderDetails) {
        OrderDTO res = new OrderDTO();
        BeanUtils.copyProperties(orderMaster, res);
        res.setOrderId(orderMaster.getId());
        res.setCreateDate(orderMaster.getCreateDate());

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

    private Map<Integer, Integer> convertListToMap(List<OrderDTO.Product> products) {
        return products.stream()
                .collect(Collectors.toMap(
                        OrderDTO.Product::getProductId,  // Key: 取 productId
                        OrderDTO.Product::getCount,      // Value: 取 count
                        Integer::sum                     // 合併重複 key 時，將 count 加總
                ));
    }
}
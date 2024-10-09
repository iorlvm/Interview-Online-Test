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
import com.apxpert.weien.service.ProductStockQueueService;
import com.apxpert.weien.utils.UserHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.apxpert.weien.utils.Constant.PRODUCT_STOCK;

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
    @Autowired
    private ProductStockQueueService productStockQueueService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private static final DefaultRedisScript<Long> CHECK_AND_DECREASE_STOCK_SCRIPT;

    static {
        CHECK_AND_DECREASE_STOCK_SCRIPT = new DefaultRedisScript<>();
        CHECK_AND_DECREASE_STOCK_SCRIPT.setLocation(new ClassPathResource("lua/checkAndDecreaseStock.lua"));
        CHECK_AND_DECREASE_STOCK_SCRIPT.setResultType(Long.class);
    }


    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        List<OrderDTO.Product> products = orderDTO.getProducts();

        if (products == null) {
            throw new IllegalArgumentException("參數錯誤: 無傳遞任何商品");
        }

        // 將請求參數合併
        Map<Integer, Integer> productMap = convertListToMap(products);

        // 準備 Lua 腳本的參數
        List<Integer> productIds = new ArrayList<>();
        List<Integer> requestedCounts = new ArrayList<>();

        for (OrderDTO.Product product : products) {
            // 使用list, 確保JSON id與數量的順序一致
            productIds.add(product.getProductId());
            requestedCounts.add(product.getCount());
        }

        // 轉成JSON格式
        String productIdsJson = null;       // 將商品 ID 轉為 JSON 字符串
        String requestedCountsJson = null;  // 將請求數量轉為 JSON 字符串
        try {
            productIdsJson = objectMapper.writeValueAsString(productIds);
            requestedCountsJson = objectMapper.writeValueAsString(requestedCounts);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 調用 Lua 腳本檢查庫存 (庫存足夠的情況下, 會直接扣減Redis中的庫存)
        Long result = stringRedisTemplate.execute(
                CHECK_AND_DECREASE_STOCK_SCRIPT,
                Collections.singletonList(PRODUCT_STOCK),
                productIdsJson, requestedCountsJson
        );

        // 檢查庫存
        boolean success = result != null && result == 1;

        List<OrderDetail> temp = new ArrayList<>(productMap.size());
        for (Map.Entry<Integer, Integer> entry : productMap.entrySet()) {
            Integer productId = entry.getKey();
            Integer count = entry.getValue();

            OrderDetail orderDetail = new OrderDetail();
            Product productFromDb = productDao.findById(productId).orElseThrow(() -> new IllegalArgumentException("參數錯誤: 不存在的商品"));

            // 訂單成功的狀態下, 扣減庫存
            if (success) {
                Integer stock = productFromDb.getStock() - count;
                // 送出一個MQ訊息更新資料庫
                productStockQueueService.sendStockUpdate(productId, stock);
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
    public OrderDTO getOrderById(Integer orderId) {
        OrderMaster orderMaster = orderMasterDao.findById(orderId).orElseThrow();
        List<OrderDetail> orderDetails = orderDetailDao.findByOrderId(orderId);
        OrderDTO orderDTO = convertToOrderDTO(orderMaster, orderDetails);
        Customer customer = customerDao.findById(orderMaster.getCustomerId()).orElseThrow();
        orderDTO.setCustomer(customer);
        return orderDTO;
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
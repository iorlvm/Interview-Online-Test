package com.apxpert.weien.controller;

import com.apxpert.weien.dto.OrderDTO;
import com.apxpert.weien.dto.Result;
import com.apxpert.weien.entity.Customer;
import com.apxpert.weien.entity.Product;
import com.apxpert.weien.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.apxpert.weien.utils.Constant.PRODUCT_STOCK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Integer productId;

    @BeforeEach
    public void setup() {
        // 這裡可以插入一些測試數據
        Product product = new Product();
        product.setName("南瓜");
        product.setStock(5);
        product.setPrice(100);
        product.setUnit("片");
        productId = productService.appProduct(product).getId();
    }

    @AfterEach
    public void tearDown() {
        // 測試結束後清空與商品庫存相關的 Redis 資料, 避免數據殘留
        stringRedisTemplate.delete(PRODUCT_STOCK);
    }

    @Test
    public void testCreateOrderSuccess() throws Exception {
        // 建立測試請求的 JSON 物件
        OrderDTO orderRequest = new OrderDTO();
        OrderDTO.Product product = new OrderDTO.Product();
        product.setProductId(productId);    // 假設的產品 ID
        product.setCount(5);                // 假設的數量
        orderRequest.setProducts(Collections.singletonList(product));

        Customer customer = new Customer();
        customer.setName("單元測試");
        orderRequest.setCustomer(customer);

        // 將請求轉換為 JSON
        String orderJson = objectMapper.writeValueAsString(orderRequest);

        // 執行請求並檢查響應
        var resultActions = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson));

        // 手動驗證響應
        String responseContent = resultActions.andReturn().getResponse().getContentAsString();
        Result result = objectMapper.readValue(responseContent, Result.class);
        Object data = result.getData();
        System.out.println(data);
        OrderDTO responseOrder = objectMapper.convertValue(data, OrderDTO.class);

        // 驗證響應
        assertEquals(true, result.getSuccess());
        assertEquals("單元測試", responseOrder.getCustomer().getName());
        assertEquals(500, responseOrder.getTotalPrice());
        assertEquals(1, responseOrder.getProducts().size());
        assertEquals("南瓜", responseOrder.getProducts().get(0).getProductName());
        assertEquals(5, responseOrder.getProducts().get(0).getCount());
    }

    @Test
    public void testCreateOrderExceedStock() throws Exception {
        // 建立測試請求的 JSON 物件
        OrderDTO orderRequest = new OrderDTO();
        OrderDTO.Product product = new OrderDTO.Product();
        product.setProductId(productId);    // 假設的產品 ID
        product.setCount(10);               // 超過訂購數量
        orderRequest.setProducts(Collections.singletonList(product));

        Customer customer = new Customer();
        customer.setName("單元測試");
        orderRequest.setCustomer(customer);

        // 將請求轉換為 JSON
        String orderJson = objectMapper.writeValueAsString(orderRequest);

        // 執行請求並檢查響應
        var resultActions = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson));

        // 手動驗證響應
        String responseContent = resultActions.andReturn().getResponse().getContentAsString();
        Result result = objectMapper.readValue(responseContent, Result.class);

        // 驗證響應
        assertEquals(false, result.getSuccess());
        assertEquals("庫存不足", result.getErrorMsg());
    }

    @Test
    public void testCreateOrderOver10() throws Exception {
        // 建立測試請求的 JSON 物件
        OrderDTO orderRequest = new OrderDTO();
        OrderDTO.Product product = new OrderDTO.Product();
        product.setProductId(productId);    // 假設的產品 ID
        product.setCount(100);              // 超過最大單次訂購數量
        orderRequest.setProducts(Collections.singletonList(product));

        Customer customer = new Customer();
        customer.setName("單元測試");
        orderRequest.setCustomer(customer);

        // 將請求轉換為 JSON
        String orderJson = objectMapper.writeValueAsString(orderRequest);

        // 執行請求並檢查響應
        var resultActions = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson));

        // 手動驗證響應
        String responseContent = resultActions.andReturn().getResponse().getContentAsString();
        Result result = objectMapper.readValue(responseContent, Result.class);

        // 驗證響應
        assertEquals(false, result.getSuccess());
        assertEquals("每種商品一次最多訂購10個", result.getErrorMsg());
    }

    @Test
    public void testCreateOrderNullProducts() throws Exception {
        // 建立測試請求的 JSON 物件
        OrderDTO orderRequest = new OrderDTO();

        Customer customer = new Customer();
        customer.setName("單元測試");
        orderRequest.setCustomer(customer);

        // 將請求轉換為 JSON
        String orderJson = objectMapper.writeValueAsString(orderRequest);

        // 執行請求並檢查響應
        var resultActions = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson));

        // 手動驗證響應
        String responseContent = resultActions.andReturn().getResponse().getContentAsString();
        Result result = objectMapper.readValue(responseContent, Result.class);

        // 驗證響應
        assertEquals(false, result.getSuccess());
        assertEquals("未傳入任何商品", result.getErrorMsg());
    }
}

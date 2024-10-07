package com.apxpert.weien.controller;

import com.apxpert.weien.dto.Result;
import com.apxpert.weien.entity.Product;
import com.apxpert.weien.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("products")
    public Result getProductList(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        Page<Product> all = productService.findAll(page, size);
        return Result.ok(all.getContent(), all.getTotalElements());
    }

}

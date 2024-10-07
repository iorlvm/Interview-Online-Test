package com.apxpert.weien.controller;

import com.apxpert.weien.dto.Result;
import com.apxpert.weien.entity.Product;
import com.apxpert.weien.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("products/{productId}")
    public Result getProduct(@PathVariable Integer productId) {
        Product product = productService.findById(productId);
        return Result.ok(product);
    }

    @PostMapping("products")
    public Result appProduct(@RequestBody Product product) {
        Product saved = productService.appProduct(product);
        return Result.ok(saved);
    }

    @PutMapping("products/{productId}")
    public Result updateProduct(@PathVariable Integer productId, @RequestBody Product product) {
        product.setId(productId);
        Product saved = productService.update(product);
        return Result.ok(saved);
    }

    @DeleteMapping("products/{productId}")
    public Result deleteProduct(@PathVariable Integer productId) {
        productService.deleteById(productId);
        return Result.ok();
    }
}

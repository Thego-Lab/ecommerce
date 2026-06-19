package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.request.ProductQueryRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.PageResult;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.ProductService;
import com.ecommerce.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ApiResponse<PageResult<Product>> page(ProductQueryRequest request) {
        return ApiResponse.success(productService.page(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> getById(@PathVariable Long id) {
        return ApiResponse.success(productService.getById(id));
    }

    @PostMapping
    public ApiResponse<Product> create(@RequestBody ProductRequest request) {
        requireSeller();
        return ApiResponse.success(productService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        requireSeller();
        productService.update(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireSeller();
        productService.delete(id);
        return ApiResponse.success();
    }

    private void requireSeller() {
        String role = UserHolder.getUser() != null ? UserHolder.getUser().getRole() : null;
        if (!"seller".equals(role)) {
            throw new BusinessException(403, "仅商家可操作，请使用商家账号登录");
        }
    }
}

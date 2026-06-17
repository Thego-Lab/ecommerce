package com.ecommerce.service;

import com.ecommerce.dto.request.ProductQueryRequest;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.PageResult;
import com.ecommerce.entity.Product;

import java.util.List;

public interface ProductService {
    PageResult<Product> page(ProductQueryRequest request);
    Product getById(Long id);
    Product create(ProductRequest request);
    void update(Long id, ProductRequest request);
    void delete(Long id);
}

package com.ecommerce.service;

import com.ecommerce.dto.request.CreateOrderRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResult;
import com.ecommerce.entity.Order;

public interface OrderService {
    OrderResponse createOrder(Long userId, CreateOrderRequest request);
    PageResult<Order> page(Long userId, Integer status, int page, int size);
    OrderResponse getOrderDetail(Long userId, Long orderId);
}

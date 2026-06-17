package com.ecommerce.controller;

import com.ecommerce.dto.request.CreateOrderRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResult;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import com.ecommerce.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> create(@RequestBody CreateOrderRequest req) {
        return ApiResponse.success(orderService.createOrder(UserHolder.getUserId(), req));
    }

    @GetMapping
    public ApiResponse<PageResult<Order>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(orderService.page(UserHolder.getUserId(), status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrderDetail(UserHolder.getUserId(), id));
    }
}

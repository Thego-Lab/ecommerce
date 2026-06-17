package com.ecommerce.controller;

import com.ecommerce.dto.request.AddCartItemRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.service.CartService;
import com.ecommerce.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        return ApiResponse.success(cartService.getCart(UserHolder.getUserId()));
    }

    @PostMapping("/items")
    public ApiResponse<Void> addItem(@RequestBody AddCartItemRequest req) {
        cartService.addItem(UserHolder.getUserId(), req);
        return ApiResponse.success();
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<Void> updateItem(@PathVariable Long itemId,
                                         @RequestBody UpdateCartItemRequest req) {
        cartService.updateItem(UserHolder.getUserId(), itemId, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> removeItem(@PathVariable Long itemId) {
        cartService.removeItem(UserHolder.getUserId(), itemId);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart() {
        cartService.clearCart(UserHolder.getUserId());
        return ApiResponse.success();
    }
}

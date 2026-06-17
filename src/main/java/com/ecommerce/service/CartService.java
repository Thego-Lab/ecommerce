package com.ecommerce.service;

import com.ecommerce.dto.request.AddCartItemRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId);
    void addItem(Long userId, AddCartItemRequest request);
    void updateItem(Long userId, Long itemId, UpdateCartItemRequest request);
    void removeItem(Long userId, Long itemId);
    void clearCart(Long userId);
}

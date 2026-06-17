package com.ecommerce.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CartItem {
    private Long id;
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private Integer checked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.ecommerce.service;

import com.ecommerce.dto.request.CreateOrderRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.*;
import com.ecommerce.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private CartMapper cartMapper;
    @Mock private CartItemMapper cartItemMapper;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CreateOrderRequest buildRequest() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setReceiverName("Alice");
        req.setReceiverPhone("13800138000");
        req.setReceiverAddress("北京市朝阳区");
        return req;
    }

    // ==================== 购物车为空 ====================

    @Test
    void createOrder_shouldThrow_whenCartEmpty() {
        when(cartMapper.selectByUserId(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, buildRequest()));
        assertEquals("购物车为空", ex.getMessage());
    }

    // ==================== 没有勾选商品 ====================

    @Test
    void createOrder_shouldThrow_whenNoCheckedItems() {
        Cart cart = new Cart();
        cart.setId(10L);
        when(cartMapper.selectByUserId(1L)).thenReturn(cart);
        when(cartItemMapper.selectCheckedByCartId(10L)).thenReturn(Collections.emptyList());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, buildRequest()));
        assertEquals("没有选中的商品", ex.getMessage());
    }

    // ==================== 库存不足 ====================

    @Test
    void createOrder_shouldThrow_whenStockInsufficient() {
        Cart cart = new Cart();
        cart.setId(10L);
        when(cartMapper.selectByUserId(1L)).thenReturn(cart);

        CartItem item = new CartItem();
        item.setProductId(5L);
        item.setQuantity(10);
        when(cartItemMapper.selectCheckedByCartId(10L)).thenReturn(List.of(item));

        Product product = new Product();
        product.setId(5L);
        product.setName("iPhone");
        product.setPrice(new BigDecimal("6999"));
        product.setStock(2); // 只剩 2 件，用户要买 10 件
        product.setStatus(1);
        when(productMapper.selectById(5L)).thenReturn(product);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, buildRequest()));
        assertEquals("商品[iPhone]库存不足", ex.getMessage());
        verify(orderMapper, never()).insert(any());
    }

    // ==================== 商品已下架 ====================

    @Test
    void createOrder_shouldThrow_whenProductOffShelf() {
        Cart cart = new Cart();
        cart.setId(10L);
        when(cartMapper.selectByUserId(1L)).thenReturn(cart);

        CartItem item = new CartItem();
        item.setProductId(5L);
        item.setQuantity(1);
        when(cartItemMapper.selectCheckedByCartId(10L)).thenReturn(List.of(item));

        Product product = new Product();
        product.setId(5L);
        product.setPrice(new BigDecimal("6999"));
        product.setStatus(0); // 已下架
        when(productMapper.selectById(5L)).thenReturn(product);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, buildRequest()));
        assertEquals("商品[5]不存在或已下架", ex.getMessage());
    }

    // ==================== 成功下单 ====================

    @Test
    void createOrder_shouldSucceed_whenAllValid() {
        Cart cart = new Cart();
        cart.setId(10L);
        when(cartMapper.selectByUserId(1L)).thenReturn(cart);

        CartItem item = new CartItem();
        item.setProductId(5L);
        item.setQuantity(2);
        when(cartItemMapper.selectCheckedByCartId(10L)).thenReturn(List.of(item));

        Product product = new Product();
        product.setId(5L);
        product.setName("iPhone 15");
        product.setPrice(new BigDecimal("6999"));
        product.setImage("https://img.example.com/iphone.jpg");
        product.setStock(100);
        product.setStatus(1);
        when(productMapper.selectById(5L)).thenReturn(product);

        when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return 1;
        });
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(productMapper.deductStock(5L, 2)).thenReturn(1);
        when(cartItemMapper.deleteCheckedByCartId(10L)).thenReturn(1);

        OrderResponse result = orderService.createOrder(1L, buildRequest());

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1, result.getStatus());
        assertEquals("Alice", result.getReceiverName());

        // 验证关键操作确实被调用了
        verify(orderMapper).insert(any(Order.class));
        verify(orderItemMapper).insert(any(OrderItem.class));
        verify(productMapper).deductStock(5L, 2);
        verify(cartItemMapper).deleteCheckedByCartId(10L);
    }
}

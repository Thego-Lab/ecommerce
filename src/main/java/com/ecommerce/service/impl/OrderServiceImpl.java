package com.ecommerce.service.impl;

import com.ecommerce.dto.request.CreateOrderRequest;
import com.ecommerce.dto.response.OrderItemResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResult;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.*;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // 1. 获取购物车
        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null) {
            throw new BusinessException("购物车为空");
        }

        // 2. 获取已勾选的购物车项
        List<CartItem> checkedItems = cartItemMapper.selectCheckedByCartId(cart.getId());
        if (checkedItems.isEmpty()) {
            throw new BusinessException("没有选中的商品");
        }

        // 3. 校验商品 + 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Product> products = new ArrayList<>();

        for (CartItem item : checkedItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getStatus() != 1) {
                throw new BusinessException("商品[" + item.getProductId() + "]不存在或已下架");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException("商品[" + product.getName() + "]库存不足");
            }
            totalAmount = totalAmount.add(
                    product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            products.add(product);
        }

        // 4. 生成订单号
        String orderNo = System.currentTimeMillis() +
                UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // 5. 插入订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(1);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setRemark(request.getRemark());
        orderMapper.insert(order);

        // 6. 插入订单明细 + 扣减库存
        for (int i = 0; i < checkedItems.size(); i++) {
            CartItem cartItem = checkedItems.get(i);
            Product product = products.get(i);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalAmount(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItemMapper.insert(orderItem);

            // 扣减库存（乐观锁检查）
            int rows = productMapper.deductStock(product.getId(), cartItem.getQuantity());
            if (rows == 0) {
                throw new BusinessException("商品[" + product.getName() + "]库存不足");
            }
        }

        // 7. 删除购物车中已下单的项
        cartItemMapper.deleteCheckedByCartId(cart.getId());

        // 8. 构建返回结果
        return buildOrderResponse(order);
    }

    @Override
    public PageResult<Order> page(Long userId, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Order> list;
        long total;

        if (status != null) {
            list = orderMapper.selectPageByUserIdAndStatus(userId, status, offset, size);
            total = orderMapper.countByUserId(userId, status);
        } else {
            list = orderMapper.selectPageByUserId(userId, offset, size);
            total = orderMapper.countByUserId(userId, null);
        }

        return new PageResult<>(total, page, size, list);
    }

    @Override
    public OrderResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(404, "订单不存在");
        }
        return buildOrderResponse(order);
    }

    private OrderResponse buildOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setReceiverAddress(order.getReceiverAddress());
        response.setRemark(order.getRemark());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemResponse ir = new OrderItemResponse();
            ir.setId(item.getId());
            ir.setProductId(item.getProductId());
            ir.setProductName(item.getProductName());
            ir.setProductImage(item.getProductImage());
            ir.setProductPrice(item.getProductPrice());
            ir.setQuantity(item.getQuantity());
            ir.setTotalAmount(item.getTotalAmount());
            itemResponses.add(ir);
        }
        response.setItems(itemResponses);
        return response;
    }
}

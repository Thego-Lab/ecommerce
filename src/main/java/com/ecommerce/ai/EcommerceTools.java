package com.ecommerce.ai;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI Function Calling 工具集 — 大模型自动调用这些方法回答用户问题
 */
@Component
@RequiredArgsConstructor
public class EcommerceTools {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    @Tool(description = "根据关键词搜索商品，返回商品列表（含名称、价格）")
    public List<Product> searchProducts(
            @ToolParam(description = "搜索关键词") String keyword) {
        return productMapper.selectPage(keyword, null, 0, 5);
    }

    @Tool(description = "查询当前登录用户的订单列表")
    public List<Order> queryMyOrders() {
        Long userId = UserHolder.getUserId();
        if (userId == null) return List.of();
        return orderMapper.selectPageByUserId(userId, 0, 10);
    }

    @Tool(description = "根据订单号查询订单详情，只能查到当前用户的订单")
    public Order queryOrder(
            @ToolParam(description = "订单号") String orderNo) {
        Long userId = UserHolder.getUserId();
        if (userId == null) return null;
        Order order = orderMapper.selectByOrderNo(orderNo);
        // 校验归属：只能查自己的订单
        if (order != null && order.getUserId().equals(userId)) {
            return order;
        }
        return null;
    }
}

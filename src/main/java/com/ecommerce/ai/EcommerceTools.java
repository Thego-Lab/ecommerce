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

@Component
@RequiredArgsConstructor
public class EcommerceTools {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    @Tool(description = "根据关键词搜索商品，返回商品名称、价格等信息。例如用户问手机"+"则传\"手机\"，用户问iPhone"+"则传\"iPhone\"")
    public List<Product> searchProducts(
            @ToolParam(description = "搜索关键词，从用户问题中提取，如：手机、iPhone、平板、衣服") String keyword) {
        return productMapper.selectPage(keyword, null, 0, 5);
    }

    @Tool(description = "查询当前登录用户的订单列表，返回订单号、金额、状态、时间")
    public List<Order> queryMyOrders() {
        Long userId = UserHolder.getUserId();
        if (userId == null) return List.of();
        return orderMapper.selectPageByUserId(userId, 0, 10);
    }

    @Tool(description = "根据订单号查询订单详情，只能查到当前登录用户的订单")
    public Order queryOrder(
            @ToolParam(description = "订单号，如：20240618123456789") String orderNo) {
        Long userId = UserHolder.getUserId();
        if (userId == null) return null;
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null && order.getUserId().equals(userId)) {
            return order;
        }
        return null;
    }
}

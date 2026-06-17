package com.ecommerce.mapper;

import com.ecommerce.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectByOrderId(Long orderId);

    @Insert("INSERT INTO order_item (order_id, product_id, product_name, product_image, " +
            "product_price, quantity, total_amount) " +
            "VALUES (#{orderId}, #{productId}, #{productName}, #{productImage}, " +
            "#{productPrice}, #{quantity}, #{totalAmount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem item);
}

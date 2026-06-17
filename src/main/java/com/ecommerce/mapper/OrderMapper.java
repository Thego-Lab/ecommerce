package com.ecommerce.mapper;

import com.ecommerce.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    // ===== 简单 SQL：注解 =====

    @Select("SELECT * FROM `order` WHERE id = #{id}")
    Order selectById(Long id);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY created_at DESC " +
            "LIMIT #{offset}, #{size}")
    List<Order> selectPageByUserId(@Param("userId") Long userId,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo}")
    Order selectByOrderNo(String orderNo);

    @Insert("INSERT INTO `order` (order_no, user_id, total_amount, status, " +
            "receiver_name, receiver_phone, receiver_address, remark) " +
            "VALUES (#{orderNo}, #{userId}, #{totalAmount}, #{status}, " +
            "#{receiverName}, #{receiverPhone}, #{receiverAddress}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    // ===== 复杂 SQL：XML =====

    long countByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    List<Order> selectPageByUserIdAndStatus(@Param("userId") Long userId,
                                            @Param("status") Integer status,
                                            @Param("offset") int offset,
                                            @Param("size") int size);
}

package com.ecommerce.mapper;

import com.ecommerce.entity.Cart;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CartMapper {

    @Select("SELECT * FROM cart WHERE user_id = #{userId}")
    Cart selectByUserId(Long userId);

    @Insert("INSERT INTO cart (user_id) VALUES (#{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Cart cart);
}

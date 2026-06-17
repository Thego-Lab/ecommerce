package com.ecommerce.mapper;

import com.ecommerce.entity.CartItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartItemMapper {

    @Select("SELECT * FROM cart_item WHERE cart_id = #{cartId}")
    List<CartItem> selectByCartId(Long cartId);

    @Select("SELECT * FROM cart_item WHERE cart_id = #{cartId} AND checked = 1")
    List<CartItem> selectCheckedByCartId(Long cartId);

    @Select("SELECT * FROM cart_item WHERE id = #{id}")
    CartItem selectById(Long id);

    @Select("SELECT * FROM cart_item WHERE cart_id = #{cartId} AND product_id = #{productId}")
    CartItem selectByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    @Insert("INSERT INTO cart_item (cart_id, product_id, quantity, checked) " +
            "VALUES (#{cartId}, #{productId}, #{quantity}, #{checked})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem item);

    @Update("UPDATE cart_item SET quantity = #{quantity}, checked = #{checked}, updated_at = NOW() WHERE id = #{id}")
    int update(CartItem item);

    @Delete("DELETE FROM cart_item WHERE id = #{id}")
    int delete(Long id);

    @Delete("DELETE FROM cart_item WHERE cart_id = #{cartId}")
    int deleteByCartId(Long cartId);

    @Delete("DELETE FROM cart_item WHERE cart_id = #{cartId} AND checked = 1")
    int deleteCheckedByCartId(Long cartId);
}
